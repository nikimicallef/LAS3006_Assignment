import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class ClientPublisher {
    private Selector clientSelector = null;
    private SocketChannel clientSocketChannel = null;

    private List<ClientCustomMessage> messagesToPublish = new ArrayList<ClientCustomMessage>();

    ClientPublisher() {
        init();
    }

    private void init() {
        if (clientSelector != null) {
            return;
        }
        if (clientSocketChannel != null) {
            return;
        }

        try {
            clientSelector = Selector.open();
            clientSocketChannel = SocketChannel.open();
            clientSocketChannel.configureBlocking(false);
            clientSocketChannel.register(clientSelector, SelectionKey.OP_CONNECT);
            clientSocketChannel.connect(new InetSocketAddress(GlobalProperties.address, GlobalProperties.port));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (GlobalProperties.debugMessages) System.out.println("ClientPublisher selector initiated.");
    }

    private void connectionManager() throws IOException {
        //if(clientSelector.selectedKeys().size() > 0){
        if (clientSelector.select() > 0) {
            final Iterator<SelectionKey> keyIterator = clientSelector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                final SelectionKey selectionKey = keyIterator.next();
                keyIterator.remove();

                if (!selectionKey.isValid()) {
                    continue;
                }

                if (selectionKey.isConnectable()) {
                    if (GlobalProperties.debugMessages) System.out.println("Creating connection to server.");
                    connect(selectionKey);
                }

                if (selectionKey.isReadable()) {
                    if (GlobalProperties.debugMessages) System.out.println("ClientPublisher is reading.");
                    read(selectionKey);
                }

                    /*if(selectionKey.isWritable()) {
                        if(GlobalProperties.debugMessages) System.out.println("ClientPublisher is writing.");
                        write(selectionKey);
                    }*/
            }
        }
        //}
    }

    private void connect(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        if (socketChannel.isConnectionPending()) {
            socketChannel.finishConnect();
        }
        socketChannel.configureBlocking(false);
        socketChannel.register(clientSelector, SelectionKey.OP_READ);
    }

    /*private void write(final SelectionKey selectionKey) throws IOException {
        if(messagesToPublish.size() > 0){
            final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            socketChannel.write(ByteBuffer.wrap(messagesToPublish.get(0).getBytes()));
            messagesToPublish.remove(0);
            //selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }*/

    private void read(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        final ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            socketChannel.read(buffer);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read!");
        }

        final ServerCustomMessage deserializedServerMessage = SerializationUtils.deserialize(buffer.array());
        if(deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PUBACK){
            System.out.println("Suback received: " + deserializedServerMessage.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        final ClientPublisher clientPublisher = new ClientPublisher();
        clientPublisher.connectionManager();

        Scanner sc = new Scanner(System.in);

        String itemToWrite;
        do {
            System.out.println("Enter an item to add");
            itemToWrite = sc.next();
            clientPublisher.messagesToPublish.add(new ClientCustomMessage(ClientMessageKey.PUBLISH, GlobalProperties.customPath, itemToWrite));

            final byte[] serializedMessage = SerializationUtils.serialize(clientPublisher.messagesToPublish.get(0));
            clientPublisher.messagesToPublish.remove(0);
            clientPublisher.clientSocketChannel.write(ByteBuffer.wrap(serializedMessage));

            //You read the ack.
            clientPublisher.connectionManager();
        } while (!itemToWrite.equalsIgnoreCase("quit"));
    }
}
