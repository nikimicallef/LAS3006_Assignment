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
//        while(true) {
//            if (selector.select() > 0) {
//                final Set<SelectionKey> selectionKeys = selector.selectedKeys();
//                final Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
//                while (keyIterator.hasNext()) {
//                    final SelectionKey selectionKey = keyIterator.next();
//                    keyIterator.remove();
//                    if(!selectionKey.isValid()){
//                        continue;
//                    }
//                    if (selectionKey.isAcceptable()) {
//                        final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
//                        final SocketChannel clientChannel = serverSocketChannel.accept();
//                        clientChannel.configureBlocking(false);
//                        clientChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
//
//                        if(TestRunner.DEBUG_MESSAGES) {
//                            System.out.println("NewClient has been registered with broker.");
//                        }
//                    }
//                    if (selectionKey.isConnectable()) {
//                        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
//                        socketChannel.finishConnect();
//                        selectionKey.interestOps(SelectionKey.OP_READ);
//
//                        if(TestRunner.DEBUG_MESSAGES) {
//                            System.out.println("NewClient registered.");
//                        }
//                    }
//                    if (selectionKey.isReadable()) {
//                        // a channel is ready for reading
//                    }
//                    if (selectionKey.isWritable()) {
//                        // a channel is ready for writing
//                    }
//                }
//            }
//        }
    }

    /*public static void main(String[] args) throws IOException {
        final MessageBrokerSelector messageBrokerSelector = new MessageBrokerSelector();
        messageBrokerSelector.acceptConnections();
    }*/
}
