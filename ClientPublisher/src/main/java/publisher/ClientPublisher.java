package publisher;

import properties.GlobalProperties;
import resources.Client;
import resources.ServerCustomMessage;
import resources.ServerMessageKey;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientPublisher extends Client {
    ClientPublisher() {
        super();
    }


    public void read() throws IOException, ClassNotFoundException {
        final ByteBuffer buffer = ByteBuffer.allocate(4096);

        try {
            getClientSocketChannel().read(buffer);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read!");
        }

        final ServerCustomMessage deserializedServerMessage = (ServerCustomMessage) GlobalProperties.deserializeMessage(buffer.array());
        if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.PUBACK) {
            System.out.println(deserializedServerMessage.getMessage());
        } else if (deserializedServerMessage.getServerMessageKey() == ServerMessageKey.CONNACK) {
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
        clientPublisher.setMessageGenerator(new PublisherMessageGenerator(clientPublisher.getClientId()));

        try {
            clientPublisher.connectionManager();
        } catch (IOException e) {
            e.printStackTrace();
            clientPublisher.getMessageGeneratorThreading().shutdown();
        }
    }
}
