import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by niki on 30/12/16.
 */
public class Server {
    private Selector serverSelector = null;
    private ServerSocketChannel serverSocketChannel = null;

    //private Map<SocketChannel,byte[]> dataTracking = new HashMap<SocketChannel, byte[]>();

    Server() {
        init();
    }

    private void init() {
        if (Global.debugMessages) System.out.println("Starting server");

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
            serverSocketChannel.bind(new InetSocketAddress(Global.address, Global.port));
            //serverSocketChannel.socket().bind(new InetSocketAddress(Global.address, Global.port));
            serverSocketChannel.register(serverSelector, serverSocketChannel.validOps());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(Global.debugMessages) System.out.println("Server Selector initiated.");
    }

    private void connectionManager() throws IOException {
        if (Global.debugMessages) System.out.println("Server waiting for a connections to handle.");

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
                        if(Global.debugMessages) System.out.println("Accepting a new client connection.");
                        acceptConnection(selectionKey);
                    }

                    if (selectionKey.isReadable()) {
                        // a channel is ready for reading
                    }

                    if (selectionKey.isWritable()) {
                        // a channel is ready for writing
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

        if (TestRunner.DEBUG_MESSAGES) System.out.println("NewClient has been registered with broker.");
    }

    /*private void write(final SelectionKey selectionKey, final S) {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

    }*/

    private void read(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

        try {
            while (readBuffer.hasRemaining()) {
                socketChannel.read(readBuffer);
            }
        } catch (IOException e) {
            System.out.println("Error reading message.");
            selectionKey.cancel();
            socketChannel.close();
            e.printStackTrace();
            return;
        }
    }

    public static void main(String[] args) throws IOException {
        final Server server = new Server();
        server.connectionManager();
    }
}
