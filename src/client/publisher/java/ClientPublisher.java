import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientPublisher extends Client{
    ClientPublisher() {
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
        if(deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PUBACK){
            System.out.println("Suback received: " + deserializedServerMessage.getMessage());
        } else if(deserializedServerMessage.getServerMessageKey() == ServerMessageKey.CONNACK) {
            System.out.println("CONNACK received: " + deserializedServerMessage.getMessage());
            this.setConnected(true);
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PINGRESP) {
            System.out.println("PINGRESP Received: " + deserializedServerMessage.getMessage());
        } else {
            System.out.println("INVALID MESSAGE! Key " + deserializedServerMessage.getServerMessageKey());
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        final ClientPublisher clientPublisher = new ClientPublisher();
        //For isConnectable + Prepare connect msg
        clientPublisher.connectionManager();
        //Send connect msg
        clientPublisher.connectionManager();
        //Receive connack
        clientPublisher.connectionManager();

        final Scanner sc = new Scanner(System.in);

        String itemToWrite;
        while(clientPublisher.isConnected()){
            System.out.println("1 = Publish. 2 = Ping. 3 = Disconnect");
            final int choice = sc.nextInt();
            if(choice == 1) {
                System.out.println("Enter an item to add");
                itemToWrite = sc.next();
                clientPublisher.getMessagesToSend().add(new ClientCustomMessage(ClientMessageKey.PUBLISH, itemToWrite, Paths.get(".")));
                clientPublisher.getSelectionKey().interestOps(SelectionKey.OP_WRITE);

                //You write.
                clientPublisher.connectionManager();

                //You read the ack.
                clientPublisher.connectionManager();
            } else if (choice == 2){
                clientPublisher.preparePingMessage();

                //You write.
                clientPublisher.connectionManager();

                //You read the ack.
                clientPublisher.connectionManager();
            } else if (choice == 3){
                clientPublisher.prepareDisconnectMessage();

                //write and dc
                clientPublisher.connectionManager();
            }
        }
    }
}
