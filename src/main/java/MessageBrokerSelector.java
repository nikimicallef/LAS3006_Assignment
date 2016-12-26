import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MessageBrokerSelector {
    private final Selector selector;

    MessageBrokerSelector() throws IOException {
        selector = Selector.open();
    }

    Selector getSelector() {
        return selector;
    }

    void acceptConnections() throws IOException {
        while(true) {
            if (selector.select() > 0) {
                final Set<SelectionKey> selectionKeys = selector.selectedKeys();
                final Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    final SelectionKey selectionKey = keyIterator.next();
                    keyIterator.remove();
                    if (selectionKey.isValid() && selectionKey.isAcceptable()) {
                        final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                        final SocketChannel clientChannel = serverSocketChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selectionKey.selector(), SelectionKey.OP_READ);

                        if(TestRunner.DEBUG_MESSAGES) {
                            System.out.println("Client has been registered with broker.");
                        }
                    }
                    if (selectionKey.isValid() && selectionKey.isConnectable()) {
                        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        socketChannel.finishConnect();
                        selectionKey.interestOps(SelectionKey.OP_READ);

                        if(TestRunner.DEBUG_MESSAGES) {
                            System.out.println("Client registered.");
                        }
                    }
                    if (selectionKey.isValid() && selectionKey.isReadable()) {
                        // a channel is ready for reading
                    }
                    if (selectionKey.isValid() && selectionKey.isWritable()) {
                        // a channel is ready for writing
                    }
                    if (!selectionKey.isValid()) {
                        // the channel has become invalid (selectionkey cancelled or channel closed)
                    }
                }
            }
        }
    }

    /*public static void main(String[] args) throws IOException {
        final MessageBrokerSelector messageBrokerSelector = new MessageBrokerSelector();
        messageBrokerSelector.acceptConnections();
    }*/
}
