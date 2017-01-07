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

public class Server {
    private Selector serverSelector = null;
    private ServerSocketChannel serverSocketChannel = null;

    private Map<Path, List<SelectionKey>> listOfSubscribers = new HashMap<>();
    private final List<Pair<SelectionKey, ServerCustomMessage>> messagesToSend = new ArrayList<>();

    Server() {
        init();
    }

    private void init() {
        if (GlobalProperties.debugMessages) System.out.println("Starting server");

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

        if (GlobalProperties.debugMessages) System.out.println("Server Selector initiated.");
    }

    private void connectionManager() throws IOException, ClassNotFoundException {
        if (GlobalProperties.debugMessages) System.out.println("Server waiting for a connections to handle.");

        while (true) {
            if (serverSelector.select() > 0) {
                final Iterator<SelectionKey> keyIterator = serverSelector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    final SelectionKey selectionKey = keyIterator.next();
                    keyIterator.remove();

                    if (!selectionKey.isValid()) {
                        System.out.println("Selection key is not valid. " + selectionKey.toString());
                    } else if (selectionKey.isAcceptable()) {
                        if (GlobalProperties.debugMessages) System.out.println("Accepting a new client connection.");
                        acceptConnection(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        if (GlobalProperties.debugMessages) System.out.println("Server is reading.");
                        read(selectionKey);
                    } else if (selectionKey.isWritable()) {
                        if (GlobalProperties.debugMessages) System.out.println("Server is writing.");
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

        if (GlobalProperties.debugMessages) System.out.println("Client has been accepted by the server..");
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

            if (listOfSubscribers.get(Paths.get(deserializedClientMessage.getPath())) != null) {
                listOfSubscribers.get(Paths.get(deserializedClientMessage.getPath())).forEach(selectionKey1 -> {
                        preparePublishMessage(selectionKey1, deserializedClientMessage.getMessage());

                });
            } else {
                System.out.println("No subscribers on path " + deserializedClientMessage.getPath());
            }

            prepareAckMessage(ServerMessageKey.PUBACK, selectionKey);
        } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.PUBREC) {
            System.out.println("Pubrec received for " + deserializedClientMessage.getClientMessageKey().toString());
        } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.SUBSCRIBE) {
            System.out.println("Subscribe to Path: " + deserializedClientMessage.getPath());

            listOfSubscribers.putIfAbsent(Paths.get(deserializedClientMessage.getPath()), new ArrayList<>());
            listOfSubscribers.get(Paths.get(deserializedClientMessage.getPath())).add(selectionKey);

            prepareAckMessage(ServerMessageKey.SUBACK, selectionKey);
        } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.CONNECT) {
            System.out.println("Connect message received from client with id " + deserializedClientMessage.getClientId());

            //TODO: Keep the client id?

            prepareAckMessage(ServerMessageKey.CONNACK, selectionKey);
        }
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
        final Server server = new Server();
        server.connectionManager();
    }
}
