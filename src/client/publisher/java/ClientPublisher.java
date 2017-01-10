import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientPublisher extends Client{
    ClientPublisher() {
        super(new PublisherMessageGenerator());
    }


    void read() throws IOException, ClassNotFoundException {
        final ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            getClientSocketChannel().read(buffer);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read!");
        }

        final ServerCustomMessage deserializedServerMessage = (ServerCustomMessage) GlobalProperties.deserializeMessage(buffer.array());
        if(deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PUBACK){
            System.out.println(deserializedServerMessage.getMessage());
        } else if(deserializedServerMessage.getServerMessageKey() == ServerMessageKey.CONNACK) {
            System.out.println(deserializedServerMessage.getMessage());
            this.setConnected(true);
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PINGRESP) {
            System.out.println(deserializedServerMessage.getMessage());
        } else {
            System.out.println("INVALID MESSAGE! Key " + deserializedServerMessage.getServerMessageKey());
        }
    }

    public static void main(String[] args) {
        final ClientPublisher clientPublisher = new ClientPublisher();

        try {
            clientPublisher.connectionManager();
        } catch (IOException e) {
            e.printStackTrace();
            clientPublisher.getMessageGeneratorThreading().shutdown();
        }
    }
}
