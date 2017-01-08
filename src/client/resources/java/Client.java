import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
    }


    void connectionManager() throws IOException, ClassNotFoundException {
        if(selectionKey != null){
            System.out.println("1= read. 4 = write. 5 = read|write. InterestOps: " + selectionKey.interestOps());
        }

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

            final Iterator<SelectionKey> keyIterator = clientSelector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                final SelectionKey selectionKey = keyIterator.next();
                keyIterator.remove();

                if (!selectionKey.isValid()) {
                    System.out.println("Selection key is not valid. " + selectionKey.toString());
                } else if (selectionKey.isConnectable()) {
                    connect();
                    prepareConnectMessage();
                } else if (selectionKey.isReadable()) {
                    read();
                } else if (selectionKey.isWritable()) {
                    write();
                }
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
        boolean disconnect = false;
        if (messagesToSend.size() > 0) {
            do {
                if(messagesToSend.get(0).getClientMessageKey() == ClientMessageKey.DISCONNECT){
                    disconnect = true;
                }
                final byte[] serializedMessage = GlobalProperties.serializeMessage(messagesToSend.get(0));
                messagesToSend.remove(0);
                clientSocketChannel.write(ByteBuffer.wrap(serializedMessage));
            }while (messagesToSend.size() > 0);
        } else {
            System.out.println("No messages to write. Going back to read.");
        }

        if(disconnect){
            disconnectClient();
        } else {
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }

    void preparePingMessage() {
        if(isConnected()){
            final ClientCustomMessage clientCustomMessage = new ClientCustomMessage(ClientMessageKey.PINGREQ, new Random().nextInt(999));

            messagesToSend.add(clientCustomMessage);

            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } else {
            System.out.println("Need to be connected to send a ping request..");
        }
    }

    void prepareDisconnectMessage() {
        if(isConnected()){
            final ClientCustomMessage clientCustomMessage = new ClientCustomMessage(ClientMessageKey.DISCONNECT);

            messagesToSend.add(clientCustomMessage);

            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } else {
            System.out.println("Need to be connected to send a disconnect request..");
        }
    }

    private void disconnectClient() throws IOException {
        if(isConnected()){
            clientSelector.close();
            clientSocketChannel.socket().close();
            clientSocketChannel.close();
            selectionKey.cancel();

            setConnected(false);
        } else {
            System.out.println("Client is not connected so it can't be disconnected.");
        }
    }
}
