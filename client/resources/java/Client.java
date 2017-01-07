import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niki on 05/01/17.
 */
public abstract class Client {
    private Selector clientSelector = null;
    private SocketChannel clientSocketChannel = null;
    private long clientSubscriberId;
    private boolean connected = false;
    private final List<ClientCustomMessage> messagesToSend = new ArrayList<>();
    private SelectionKey selectionKey = null;

    public Selector getClientSelector() {
        return clientSelector;
    }

    public SocketChannel getClientSocketChannel() {
        return clientSocketChannel;
    }

    public long getClientSubscriberId() {
        return clientSubscriberId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public List<ClientCustomMessage> getMessagesToSend() {
        return messagesToSend;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public Client() {
        clientSubscriberId = System.currentTimeMillis();

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

        if (GlobalProperties.debugMessages) System.out.println("Client selector initiated.");
    }


    void connectionManager() throws IOException, ClassNotFoundException {
        if (clientSelector.select() > 0) {
            if (clientSelector.selectedKeys().size() > 1) {
                throw new IllegalArgumentException("This selector has more than one selection key! VIOLATION OBSERVED!");
            } else if (selectionKey == null) {
                //Since a client is connected to only one server it has only one selector so the selection key is global
                //(and we always modify it via interest ops).
                // Here we set the global selection key during the first run
                // (which was spawned via the register in method init()).
                selectionKey = clientSelector.selectedKeys().iterator().next();
            }

            if (!selectionKey.isValid()) {
                System.out.println("Selection key is not valid. " + selectionKey.toString());
            } else if (selectionKey.isConnectable()) {
                if (GlobalProperties.debugMessages) System.out.println("Creating connection to server.");
                connect();
                prepareConnectMessage();
            } else if (selectionKey.isReadable()) {
                if (GlobalProperties.debugMessages) System.out.println("Client is reading.");
                read();
            } else if (selectionKey.isWritable()) {
                if (GlobalProperties.debugMessages) System.out.println("Client is writing.");
                write();
            }
        }
    }

    private void prepareConnectMessage() throws IOException {
        if(!isConnected()) {
            final ClientCustomMessage clientCustomMessage = new ClientCustomMessage(ClientMessageKey.CONNECT, clientSubscriberId);

            messagesToSend.add(clientCustomMessage);

            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } else {
            System.out.println("Already connected.");
        }
    }

    private void connect() throws IOException {
        if (clientSocketChannel.isConnectionPending()) {
            clientSocketChannel.finishConnect();
        }
        clientSocketChannel.configureBlocking(false);
        selectionKey.interestOps(SelectionKey.OP_READ);
        System.out.println("channel is " + selectionKey.channel().toString());
    }

    abstract void read() throws IOException, ClassNotFoundException;

    private void write() throws IOException {
        if (messagesToSend.size() > 0) {
            do {
                final byte[] serializedMessage = GlobalProperties.serializeMessage(messagesToSend.get(0));
                messagesToSend.remove(0);
                clientSocketChannel.write(ByteBuffer.wrap(serializedMessage));
            }while (messagesToSend.size() > 0);
        } else {
            System.out.println("No messages to write. Going back to read.");
        }

        selectionKey.interestOps(SelectionKey.OP_READ);
    }
}
