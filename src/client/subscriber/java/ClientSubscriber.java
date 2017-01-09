import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class ClientSubscriber extends Client {
    ClientSubscriber(final MessageGenerator messageGenerator) {
        super(messageGenerator);
    }

    void read() throws IOException, ClassNotFoundException {
        final ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            getClientSocketChannel().read(buffer);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read!");
        }

        final ServerCustomMessage deserializedServerMessage = (ServerCustomMessage) GlobalProperties.deserializeMessage(buffer.array());

        if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.SUBACK) {
            System.out.println("Subscribe acknowledgement received.");
            getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PUBLISH) {
            System.out.println("Data received: " + deserializedServerMessage.getMessage());
            prepareAckForWrite(ClientMessageKey.PUBREC, deserializedServerMessage);
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.CONNACK) {
            System.out.println("CONNACK received");
            this.setConnected(true);
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.UNSUBACK) {
            System.out.println("Unsubscribe acknowledgement received.");
            getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PINGRESP) {
            System.out.println("PINGRESP Received: " + deserializedServerMessage.getMessage());
            getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        } else {
            System.out.println("INVALID MESSAGE! Key " + deserializedServerMessage.getServerMessageKey());
            getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void prepareAckForWrite(final ClientMessageKey clientMessageKey, final ServerCustomMessage dataReceived) throws IOException {
        if (this.isConnected()) {
            getMessageGeneratorThreading().getMessageGenerator().getMessagesToWrite().add(new ClientCustomMessage(clientMessageKey, "Ack for " + dataReceived.getServerMessageKey().toString()));
            getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        } else {
            System.out.println("Client is not connected yet and thus it can't write.");
        }
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        final ClientSubscriber clientSubscriber = new ClientSubscriber(new SubscriberMessageGenerator());

        clientSubscriber.connectionManager();
    }
}
