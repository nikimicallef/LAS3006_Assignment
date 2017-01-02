import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

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
            clientSocketChannel.connect(new InetSocketAddress(GlobalProperties.address, GlobalProperties.port));
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(GlobalProperties.debugMessages) System.out.println("Client selector initiated.");
    }

    private void connectionManager() throws IOException {
        if(GlobalProperties.debugMessages) System.out.println("Client waiting for a connection to handle.");

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
                        if(GlobalProperties.debugMessages) System.out.println("Accepting server connection client side.");
                        connect(selectionKey);
                    }

                    if(selectionKey.isReadable()) {
                        if(GlobalProperties.debugMessages) System.out.println("Client is reading.");
                        read(selectionKey);
                    }

                    if(selectionKey.isWritable()) {
                        if(GlobalProperties.debugMessages) System.out.println("Client is writing.");
                        write(selectionKey);
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

    private void write(final SelectionKey selectionKey/*, final String message*/) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        socketChannel.write(ByteBuffer.wrap("Hardcoded test msg".getBytes()));

        selectionKey.interestOps(SelectionKey.OP_READ);
    }

    private void read(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        final ByteBuffer readByteBuffer = ByteBuffer.allocate(1024);
        //readByteBuffer.clear();
        int bytesRead = 0;
        try {
            while (readByteBuffer.hasRemaining()) {
                bytesRead += socketChannel.read(readByteBuffer);
            }
        } catch (IOException e) {
            System.out.println("Client encountered an error reading.");
            socketChannel.close();
            selectionKey.cancel();
            e.printStackTrace();
            return;
        }

        if(bytesRead > 0 ) {
            readByteBuffer.flip();

            final byte[] dataRead = new byte[1024];
            readByteBuffer.get(dataRead, 0, bytesRead);
            System.out.println("Read: " + new String(dataRead));
        } else {
            System.out.println("Nothing read from server.");
            socketChannel.close();
            selectionKey.cancel();
        }
    }

    public static void main(String[] args) throws IOException {
        final Client client = new Client();
        client.connectionManager();
    }
}
