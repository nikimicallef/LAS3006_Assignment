import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    private Selector serverSelector = null;
    private ServerSocketChannel serverSocketChannel = null;

    private Map<Path, List<SelectionKey>> listOfSubscribers = new HashMap<>();
    private final List<Pair<SelectionKey, ServerCustomMessage>> messagesToSend = new ArrayList<>();

    Server(final boolean initialiseServer) {
        if (initialiseServer) {
            init();
        }
    }

    private void init() {
        if (serverSelector != null) {
            return;
        }
        if (serverSocketChannel != null) {
            return;
        }

        try {
            serverSelector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(GlobalProperties.address, GlobalProperties.port));
            serverSocketChannel.register(serverSelector, serverSocketChannel.validOps());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectionManager() throws IOException, ClassNotFoundException {
        while (true) {
            if (serverSelector.select() > 0) {
                final Iterator<SelectionKey> keyIterator = serverSelector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    final SelectionKey selectionKey = keyIterator.next();
                    keyIterator.remove();

                    if (!selectionKey.isValid()) {
                        System.out.println("Selection key is not valid. " + selectionKey.toString());
                    } else if (selectionKey.isAcceptable()) {
                        acceptConnection(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        read(selectionKey);
                    } else if (selectionKey.isWritable()) {
                        write(selectionKey);
                    }
                }
            }
        }
    }

    private void acceptConnection(final SelectionKey selectionKey) throws IOException {
        final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        final SocketChannel clientSocketChannel = serverSocketChannel.accept();
        clientSocketChannel.configureBlocking(false);
        clientSocketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
    }

    private void write(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        final List<Pair<SelectionKey, ServerCustomMessage>> messagesToRemove = new ArrayList<>();

        messagesToSend.stream().filter(messageToSend -> messageToSend.getFirst() == selectionKey).forEach(messageToSend -> {
            System.out.println("Writing message " + messageToSend.getSecond().getMessage() + " to selection key " + messageToSend.getFirst().channel().toString());
            final byte[] serializedMessage;
            try {
                serializedMessage = GlobalProperties.serializeMessage(messageToSend.getSecond());
                messagesToRemove.add(messageToSend);
                socketChannel.write(ByteBuffer.wrap(serializedMessage));
            } catch (IOException e) {
                System.out.println("Failed to write.");
                e.printStackTrace();
            }
        });

        messagesToRemove.forEach(messagesToSend::remove);

        selectionKey.interestOps(SelectionKey.OP_READ);
    }

    private void read(final SelectionKey selectionKey) throws IOException, ClassNotFoundException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        final ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            socketChannel.read(buffer);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read!");
        }

        final ClientCustomMessage deserializedClientMessage = (ClientCustomMessage) GlobalProperties.deserializeMessage(buffer.array());

        if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.PUBLISH) {
            System.out.println("Data read: " + deserializedClientMessage.getMessage());

            prepareAckMessage(ServerMessageKey.PUBACK, selectionKey);

            final List<Path> validPaths = listOfSubscribers.keySet().stream().filter(path -> pathsMatch(deserializedClientMessage.getPath(), path)).collect(Collectors.toList());

            validPaths.forEach(path -> {
                final List<SelectionKey> selectionKeys = listOfSubscribers.get(path);
                selectionKeys.forEach(selectionKey1 -> preparePublishMessage(selectionKey1, deserializedClientMessage.getMessage()));
            });
        } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.PUBREC) {
            System.out.println("Pubrec received for " + deserializedClientMessage.getClientMessageKey().toString());
        } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.SUBSCRIBE) {
            System.out.println("Subscribe to Path: " + deserializedClientMessage.getPath());

            final boolean pathValid = pathChecker(deserializedClientMessage.getPath());

            if (pathValid) {
                listOfSubscribers.putIfAbsent(Paths.get(deserializedClientMessage.getPath()), new ArrayList<>());
                listOfSubscribers.get(Paths.get(deserializedClientMessage.getPath())).add(selectionKey);
            }

            prepareAckMessage(ServerMessageKey.SUBACK, selectionKey);
        } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.CONNECT) {
            System.out.println("Connect message received from client with id " + deserializedClientMessage.getClientId());

            //TODO: Keep the client id?

            prepareAckMessage(ServerMessageKey.CONNACK, selectionKey);
        } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.UNSUBSCRIBE) {
            if (listOfSubscribers.get(Paths.get(deserializedClientMessage.getPath())) != null) {
                listOfSubscribers.get(Paths.get(deserializedClientMessage.getPath())).remove(selectionKey);
            } else {
                System.out.println("No subscribers on path " + deserializedClientMessage.getPath());
            }

            prepareAckMessage(ServerMessageKey.UNSUBACK, selectionKey);
        }
    }

    boolean pathsMatch(final String inputtedPath, final Path pathInHashMap) {
        final String pathInHashMapAsString = pathInHashMap.toString();

        final String[] inputtedPathSplit = inputtedPath.split("/");
        final String[] pathInHashMapSplit = pathInHashMapAsString.split("/");

        for (int counter = 0; counter < pathInHashMapSplit.length; counter++) {
            if (pathInHashMapSplit[counter].equals("#") || (inputtedPathSplit.length > counter && inputtedPathSplit[counter].equals("#"))) {
                return true;
            } else if (pathInHashMapSplit[counter].equals("+") || ((inputtedPathSplit.length > counter && inputtedPathSplit[counter].equals("+")))) {

            } else if (inputtedPathSplit.length > counter && !pathInHashMapSplit[counter].equals(inputtedPathSplit[counter])) {
                return false;
            }
        }

        return inputtedPathSplit.length == pathInHashMapSplit.length;
    }

    boolean pathChecker(final String path) {
        final String[] pathLevels = path.split("/");

        if (Arrays.stream(pathLevels).filter(level -> level.length() == 0).collect(Collectors.toList()).size() > 0) {
            System.out.println("Path " + path + " invalid since once or more levels is empty");
            return false;
        } else if (path.charAt(0) == '/') {
            System.out.println("Path " + path + " invalid since it starts with a /");
            return false;
        } else if (path.charAt(path.length() - 1) == '/') {
            System.out.println("Path " + path + " invalid since it ends with a /.");
            return false;
        } else if (path.contains(" ")) {
            System.out.println("Path " + path + " invalid since it contains a space.");
            return false;
        } else if (Arrays.stream(pathLevels).filter(level -> level.length() > 1 && (level.contains("+") || level.contains("#"))).collect(Collectors.toList()).size() > 0) {
            System.out.println("Path " + path + " invalid since it contains a + or a # within a level");
            return false;
        } else if (Arrays.stream(pathLevels).filter(level -> !(pathLevels[pathLevels.length - 1].equals(level))).filter(item -> item.contains("#")).collect(Collectors.toList()).size() > 0) {
            System.out.println("Path " + path + " invalid since the wildcard # can only be used at the end.");
            return false;
        }
        return true;
    }

    private void prepareAckMessage(final ServerMessageKey serverMessageKey, final SelectionKey selectionKey) {
        messagesToSend.add(new Pair<>(selectionKey, new ServerCustomMessage(serverMessageKey, "Ack of type " + serverMessageKey)));

        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    private void preparePublishMessage(final SelectionKey selectionKey, final String message) {
        messagesToSend.add(new Pair<>(selectionKey, new ServerCustomMessage(ServerMessageKey.PUBLISH, message)));

        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        final Server server = new Server(true);
        server.connectionManager();
    }
}
