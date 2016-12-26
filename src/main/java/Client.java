import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Client {
    public Client(final int port, final String address) throws IOException {
        final SocketChannel socketChannel = SocketChannel.open();
        //socketChannel.configureBlocking(false);
        /*final SelectionKey selectionKey =*/ //socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.connect(new InetSocketAddress(address, port));
    }
}
