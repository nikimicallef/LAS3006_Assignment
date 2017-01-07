import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by niki on 03/01/17.
 */
public class ClientSubscriber extends Client {
    ClientSubscriber() {
        super();
    }

    void read() throws IOException, ClassNotFoundException {
        //final SocketChannel socketChannel = (SocketChannel) getSelectionKey().channel();

        final ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            getClientSocketChannel().read(buffer);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read!");
        }

        final ServerCustomMessage deserializedServerMessage = (ServerCustomMessage) GlobalProperties.deserializeMessage(buffer.array());

        if(deserializedServerMessage.getServerMessageKey() == ServerMessageKey.SUBACK){
            System.out.println("Subscribe acknowledgement received.");
            getSelectionKey().interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }else if(deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PUBLISH){
            System.out.println("Data received: " + deserializedServerMessage.getMessage());
            prepareAckForWrite(ClientMessageKey.PUBREC, deserializedServerMessage);
        } else if(deserializedServerMessage.getServerMessageKey() == ServerMessageKey.CONNACK) {
            System.out.println("CONNACK received");
            this.setConnected(true);
        }
    }

    private void prepareAckForWrite(final ClientMessageKey clientMessageKey, final ServerCustomMessage dataReceived) throws IOException {
        if(this.isConnected()) {
            getMessagesToSend().add(new ClientCustomMessage(clientMessageKey, "Ack for " + dataReceived.getServerMessageKey().toString()));
            getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        } else {
            System.out.println("Client is not connected yet and thus it can't write.");
        }
    }

    private void prepareSubscribeToPath(final Path path) throws IOException {
        if(this.isConnected()) {
            this.getMessagesToSend().add(new ClientCustomMessage(ClientMessageKey.SUBSCRIBE, path));
            getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        } else {
            System.out.println("Client is not connected yet and thus it can't write.");
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        final ClientSubscriber clientSubscriber = new ClientSubscriber();
        //For isConnectable + Prepare connect msg
        clientSubscriber.connectionManager();
        //Send connect msg
        clientSubscriber.connectionManager();
        //Receive connack
        clientSubscriber.connectionManager();

        //System.out.println("Subscribe to path .");
        clientSubscriber.prepareSubscribeToPath(Paths.get("."));

        while(true) {
            clientSubscriber.connectionManager();
        }
    }
}
