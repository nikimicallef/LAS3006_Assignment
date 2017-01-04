import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.*;

public class Server {
    private Selector serverSelector = null;
    private ServerSocketChannel serverSocketChannel = null;

    private Map<CustomPath, List<SocketChannel>> listOfSubscribers = new HashMap<>();

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

    private void connectionManager() throws IOException {
        if (GlobalProperties.debugMessages) System.out.println("Server waiting for a connections to handle.");

        while (true) {
            if (serverSelector.select() > 0) {
                final Iterator<SelectionKey> keyIterator = serverSelector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    final SelectionKey selectionKey = keyIterator.next();
                    keyIterator.remove();

                    if (selectionKey.isAcceptable()) {
                        if (GlobalProperties.debugMessages) System.out.println("Accepting a new client connection.");
                        acceptConnection(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        if (GlobalProperties.debugMessages) System.out.println("Server is reading.");
                        read(selectionKey);
                    }

                    /*else if (selectionKey.isWritable()) {
                        if(GlobalProperties.debugMessages) System.out.println("Server is writing.");
                        write(selectionKey);
                    }*/

                    else if (!selectionKey.isValid()) {
                        System.out.println("Selection key is not valid. " + selectionKey.toString());
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

    /*private void write(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        final byte[] dataToWrite = dataTracking.get(socketChannel);
        dataTracking.remove(socketChannel);

        socketChannel.write(ByteBuffer.wrap(dataToWrite));
        selectionKey.interestOps(SelectionKey.OP_READ);
    }*/

    private void read(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        final ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            socketChannel.read(buffer);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read!");
        }

        final ClientCustomMessage deserializedClientMessage = SerializationUtils.deserialize(buffer.array());

        if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.PUBLISH) {
            System.out.println("Data read: " + deserializedClientMessage.getMessage());

            sendAck(ServerMessageKey.PUBACK, socketChannel, deserializedClientMessage);

            if (listOfSubscribers.get(deserializedClientMessage.getPath()) != null) {
                listOfSubscribers.get(deserializedClientMessage.getPath()).forEach(socketChannel1 -> {
                    try {
                        publishMessage(socketChannel1, deserializedClientMessage.getMessage());
                    } catch (IOException e) {
                        System.out.println("Error publishing msg to " + socketChannel1.toString());
                    }
                });
            } else {
                System.out.println("No subscribers on path " + deserializedClientMessage.getPath().toString());
            }
        } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.PUBREC) {
            System.out.println("Pubrec received for " + deserializedClientMessage.getClientMessageKey().toString());
        } else if(deserializedClientMessage.getClientMessageKey() == ClientMessageKey.SUBSCRIBE){
            System.out.println("Subscribe to Path: " + deserializedClientMessage.getPath().getPath());

            listOfSubscribers.putIfAbsent(deserializedClientMessage.getPath(), new ArrayList<>());
            listOfSubscribers.get(deserializedClientMessage.getPath()).add(socketChannel);

            sendAck(ServerMessageKey.SUBACK, socketChannel, deserializedClientMessage);
        }
    }

    private void sendAck(final ServerMessageKey serverMessageKey, final SocketChannel socketChannel, final ClientCustomMessage dataReceived) throws IOException {
        final byte[] serializedMessage = SerializationUtils.serialize(new ServerCustomMessage(serverMessageKey, "Ack for" + dataReceived.getClientMessageKey().toString()));
        socketChannel.write(ByteBuffer.wrap(serializedMessage));
    }

    private void publishMessage(final SocketChannel socketChannel, final String message) throws IOException {
        final byte[] serializedMessage = SerializationUtils.serialize(new ServerCustomMessage(ServerMessageKey.PUBLISH, "Publishing " + message));
        socketChannel.write(ByteBuffer.wrap(serializedMessage));
    }

    public static void main(String[] args) throws IOException {
        final Server server = new Server();
        server.connectionManager();
    }
}
