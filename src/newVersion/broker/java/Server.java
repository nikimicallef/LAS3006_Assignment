import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by niki on 30/12/16.
 */
public class Server {
    private ServerSocketChannel serverSocketChannel = null;
    private Selector serverSelector = null;

    //private Map<SocketChannel,byte[]> dataTracking = new HashMap<SocketChannel, byte[]>();

    Server() {
        init();
    }

    private void init() {
        if(Global.debugMessages) System.out.println("Starting server");

        if (serverSelector != null) return;
        if (serverSocketChannel != null) return;

        try {
            serverSelector= Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(Global.address, Global.port));
            //serverSocketChannel.socket().bind(new InetSocketAddress(Global.address, Global.port));
            serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectionHandler() throws IOException {
        if(Global.debugMessages) System.out.println("Waiting for a connections to handle.");

        while(true) {
            if (serverSelector.select() > 0) {
                final Iterator<SelectionKey> keyIterator = serverSelector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    final SelectionKey selectionKey = keyIterator.next();
                    keyIterator.remove();

                    if(!selectionKey.isValid()){
                        continue;
                    }

                    if (selectionKey.isAcceptable()) {
                        System.out.println("Accepting a new client connection.");
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

        if(TestRunner.DEBUG_MESSAGES) System.out.println("NewClient has been registered with broker.");
    }

    public static void main(String[] args) throws IOException {
        final Server server= new Server();
        server.connectionHandler();
    }
}
