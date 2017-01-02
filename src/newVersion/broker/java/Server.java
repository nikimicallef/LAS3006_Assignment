import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server {
    private Selector serverSelector = null;
    private ServerSocketChannel serverSocketChannel = null;

    private Map<SocketChannel,byte[]> dataTracking = new HashMap<SocketChannel, byte[]>();

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
            //serverSocketChannel.socket().bind(new InetSocketAddress(GlobalProperties.address, GlobalProperties.port));
            serverSocketChannel.register(serverSelector, serverSocketChannel.validOps());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(GlobalProperties.debugMessages) System.out.println("Server Selector initiated.");
    }

    private void connectionManager() throws IOException {
        if (GlobalProperties.debugMessages) System.out.println("Server waiting for a connections to handle.");

        while (true) {
            if (serverSelector.select() > 0) {
                final Iterator<SelectionKey> keyIterator = serverSelector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    final SelectionKey selectionKey = keyIterator.next();
                    keyIterator.remove();

                    if (!selectionKey.isValid()) {
                        continue;
                    }

                    if (selectionKey.isAcceptable()) {
                        if(GlobalProperties.debugMessages) System.out.println("Accepting a new client connection.");
                        acceptConnection(selectionKey);
                    }

                    if (selectionKey.isReadable()) {
                        if(GlobalProperties.debugMessages) System.out.println("Server is reading.");
                        read(selectionKey);
                    }

                    if (selectionKey.isWritable()) {
                        if(GlobalProperties.debugMessages) System.out.println("Server is writing.");
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

        if (TestRunner.DEBUG_MESSAGES) System.out.println("Client has been accepted by the server..");

        final byte[] connectionEstablished = "Connection established".getBytes();
        dataTracking.put(clientSocketChannel, connectionEstablished);
    }

    private void write(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        final byte[] dataToWrite = dataTracking.get(socketChannel);
        dataTracking.remove(socketChannel);

        socketChannel.write(ByteBuffer.wrap(dataToWrite));
        selectionKey.interestOps(SelectionKey.OP_READ);
    }

    private void read(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int bytesRead = 0;

        try {
            int oldBytesRead = bytesRead;
            while (readBuffer.hasRemaining()) {
                bytesRead += socketChannel.read(readBuffer);
                //TODO: The below should be work using the hasRemaining() method only.
                if(bytesRead == oldBytesRead){
                    break;
                } else {
                    oldBytesRead = bytesRead;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading message.");
            selectionKey.cancel();
            socketChannel.close();
            e.printStackTrace();
            return;
        }

        if(bytesRead > 0) {
            readBuffer.flip();
            final byte[] dataRead = new byte[1024];
            readBuffer.get(dataRead, 0, bytesRead);
            System.out.println("Data read: " + new String(dataRead));

            echo(selectionKey, dataRead);
        } else {
            System.out.println("Nothing was read.");
            socketChannel.close();
            selectionKey.cancel();
        }
    }

    private void echo(final SelectionKey selectionKey, final byte[] dataRead){
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        dataTracking.put(socketChannel, dataRead);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    public static void main(String[] args) throws IOException {
        final Server server = new Server();
        server.connectionManager();
    }
}
