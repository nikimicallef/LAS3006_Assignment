import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by niki on 30/12/16.
 */
public class Client {
    private Selector clientSelector = null;
    private SocketChannel clientSocketChannel = null;

    Client() {
        init();
    }

    private void init() {
        if (clientSelector != null) {
            return;
        }
        if (clientSocketChannel != null) {
            return;
        }

        try{
            clientSelector = Selector.open();
            clientSocketChannel = SocketChannel.open();
            clientSocketChannel.configureBlocking(false);
            clientSocketChannel.register(clientSelector, SelectionKey.OP_CONNECT);
            clientSocketChannel.connect(new InetSocketAddress(Global.address, Global.port));
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(Global.debugMessages) System.out.println("Client selector initiated.");
    }

    private void connectionManager() throws IOException {
        if(Global.debugMessages) System.out.println("Client waiting for a connection to handle.");

        while(true){
            if(clientSelector.select() > 0){
                final Iterator<SelectionKey> keyIterator = clientSelector.selectedKeys().iterator();

                while (keyIterator.hasNext()){
                    final SelectionKey selectionKey = keyIterator.next();
                    keyIterator.remove();

                    if(!selectionKey.isValid()) {
                        continue;
                    }

                    if(selectionKey.isConnectable()){
                        if(Global.debugMessages) System.out.println("Connecting to server.");
                        connect(selectionKey);
                    }

                    if(selectionKey.isReadable()) {
                        // a channel is ready for reading
                    }

                    if(selectionKey.isWritable()) {
                        // a channel is ready for writing
                    }
                }
            }
        }
    }

    private void connect(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        if(socketChannel.isConnectionPending()){
            socketChannel.finishConnect();
        }
        socketChannel.configureBlocking(false);
        socketChannel.register(clientSelector, SelectionKey.OP_WRITE);
    }

    public static void main(String[] args) {
        final Client client = new Client();
    }
}
