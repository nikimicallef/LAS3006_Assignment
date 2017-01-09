import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by niki on 05/01/17.
 */
public abstract class Client {
    private Selector clientSelector = null;
    private SocketChannel clientSocketChannel = null;
    private String clientSubscriberId;
    private boolean connected = false;
    private SelectionKey selectionKey = null;
    private MessageGeneratorThreading messageGeneratorThreading;
    private boolean monitorSelectionKeys = true;

    public MessageGeneratorThreading getMessageGeneratorThreading() {
        return messageGeneratorThreading;
    }

    public Selector getClientSelector() {
        return clientSelector;
    }

    public SocketChannel getClientSocketChannel() {
        return clientSocketChannel;
    }

    /*public long getClientSubscriberId() {
        return clientSubscriberId;
    }*/

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public Client(final MessageGenerator messageGenerator) {
        clientSubscriberId = UUID.randomUUID().toString();

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

        messageGeneratorThreading = new MessageGeneratorThreading(messageGenerator);
    }


    void connectionManager() throws IOException, ClassNotFoundException {
        if(selectionKey != null){
            System.out.println("1= read. 4 = write. 5 = read|write. InterestOps: " + selectionKey.interestOps());
        }

        while(monitorSelectionKeys) {
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
                    } else if (selectionKey.isValid() && selectionKey.isConnectable()) {
                        connect();
                        prepareConnectMessage();
                    } else if (selectionKey.isValid() &&selectionKey.isReadable()) {
                        read();
                    } else if (selectionKey.isValid() &&selectionKey.isWritable()) {
                        write();
                    }
                }
            }
        }
    }

    private void prepareConnectMessage() throws IOException {
        if(!isConnected()) {
            final ClientCustomMessage clientCustomMessage = new ClientCustomMessage(ClientMessageKey.CONNECT, clientSubscriberId);

            messageGeneratorThreading.getMessageGenerator().getMessagesToWrite().add(clientCustomMessage);

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
        if (messageGeneratorThreading.getMessageGenerator().getMessagesToWrite().size() > 0) {
            do {
                if(messageGeneratorThreading.getMessageGenerator().getMessagesToWrite().get(0).getClientMessageKey() == ClientMessageKey.DISCONNECT){
                    disconnect = true;
                }

                if(!isConnected() && messageGeneratorThreading.getMessageGenerator().getMessagesToWrite().get(0).getClientMessageKey() != ClientMessageKey.CONNECT){
                    messageGeneratorThreading.getMessageGenerator().getMessagesToWrite().remove(0);
                } else {
                    System.out.println("Writing msg of type " + messageGeneratorThreading.getMessageGenerator().getMessagesToWrite().get(0).getClientMessageKey());
                    final byte[] serializedMessage = GlobalProperties.serializeMessage(messageGeneratorThreading.getMessageGenerator().getMessagesToWrite().get(0));
                    messageGeneratorThreading.getMessageGenerator().getMessagesToWrite().remove(0);
                    clientSocketChannel.write(ByteBuffer.wrap(serializedMessage));
                }
            }while (messageGeneratorThreading.getMessageGenerator().getMessagesToWrite().size() > 0);
        }

        if(disconnect){
            disconnectClient();
        } else {
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    private void disconnectClient() throws IOException {
        if(isConnected()){
            clientSelector.close();
            clientSocketChannel.socket().close();
            clientSocketChannel.close();
            selectionKey.cancel();

            setConnected(false);

            monitorSelectionKeys = false;

            messageGeneratorThreading.shutdown();
        } else {
            System.out.println("Client is not connected so it can't be disconnected.");
        }
    }
}
