import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientSubscriber extends Client {
    ClientSubscriber() {
        super();
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
            //getSelectionKey().interestOps(SelectionKey.OP_READ);
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PUBLISH) {
            System.out.println("Data received: " + deserializedServerMessage.getMessage());
            prepareAckForWrite(ClientMessageKey.PUBREC, deserializedServerMessage);
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.CONNACK) {
            System.out.println("CONNACK received");
            this.setConnected(true);
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.UNSUBACK) {
            System.out.println("Unsubscribe acknowledgement received.");
            //getSelectionKey().interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PINGRESP) {
            System.out.println("PINGRESP Received: " + deserializedServerMessage.getMessage());
        } else {
            System.out.println("INVALID MESSAGE! Key " + deserializedServerMessage.getServerMessageKey());
        }
    }

    private void prepareAckForWrite(final ClientMessageKey clientMessageKey, final ServerCustomMessage dataReceived) throws IOException {
        if (this.isConnected()) {
            getMessagesToSend().add(new ClientCustomMessage(clientMessageKey, "Ack for " + dataReceived.getServerMessageKey().toString()));
            getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        } else {
            System.out.println("Client is not connected yet and thus it can't write.");
        }
    }

    private void prepareSubscribeToPath(final Path path) throws IOException {
        if (this.isConnected()) {
            this.getMessagesToSend().add(new ClientCustomMessage(ClientMessageKey.SUBSCRIBE, path));
            getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        } else {
            System.out.println("Client is not connected yet and thus it can't write.");
        }
    }

    private void prepareUnsubFromPath(final Path path) throws IOException {
        if (this.isConnected()) {
            this.getMessagesToSend().add(new ClientCustomMessage(ClientMessageKey.UNSUBSCRIBE, path));
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

        final Scanner sc = new Scanner(System.in);

        while (clientSubscriber.isConnected()) {
            System.out.println("1. Subscribe on .. 2. Unsubscribe. 3. Reading. 4. Ping. 5. Disconnect.");
            final int choice = sc.nextInt();
            if (choice == 1) {
                clientSubscriber.prepareSubscribeToPath(Paths.get("."));

                //Write
                clientSubscriber.connectionManager();

                //Suback
                clientSubscriber.connectionManager();
            } else if (choice == 2) {
                clientSubscriber.prepareUnsubFromPath(Paths.get("."));
                //write
                clientSubscriber.connectionManager();
                //ack
                clientSubscriber.connectionManager();
            } else if (choice == 3) {
                //read
                clientSubscriber.connectionManager();
                //readreq
                clientSubscriber.connectionManager();
            } else if (choice == 4) {
                clientSubscriber.preparePingMessage();
                //write
                clientSubscriber.connectionManager();
                //ack
                clientSubscriber.connectionManager();
            } else if (choice == 5) {
                clientSubscriber.prepareDisconnectMessage();

                clientSubscriber.connectionManager();
            }
        }
    }
}
