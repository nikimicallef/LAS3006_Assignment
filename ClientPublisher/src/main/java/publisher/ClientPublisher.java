package publisher;

import resources.Client;
import resources.PathParsing;
import resources.ServerCustomMessage;
import resources.ServerMessageKey;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;

import static resources.CustomSerializer.deserializeMessage;

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

        final ServerCustomMessage deserializedServerMessage = (ServerCustomMessage) deserializeMessage(buffer.array());
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

        if (args.length > 0) {
            if (PathParsing.pathChecker(args[0])) {
                clientPublisher.setMessageGenerator(new PublisherMessageGenerator(clientPublisher.getClientId(), args[0]));
                System.out.println("Client will be using path " + args[0]);
            } else {
                System.out.println("Inputted path is not valid. Starting without hardcoded path.");
                clientPublisher.setMessageGenerator(new PublisherMessageGenerator(clientPublisher.getClientId()));
            }
        } else {
            clientPublisher.setMessageGenerator(new PublisherMessageGenerator(clientPublisher.getClientId()));
            System.out.println("Client will be generating a path.");
        }

        try {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(clientPublisher.getMessageGeneratorThreading().getMessageGenerator(), clientPublisher.getMessageGeneratorThreading().getMessageGenerator().getObjectName());
            clientPublisher.connectionManager();
        } catch (IOException | MalformedObjectNameException | NotCompliantMBeanException | InstanceAlreadyExistsException | MBeanRegistrationException e) {
            e.printStackTrace();
            clientPublisher.getMessageGeneratorThreading().shutdown();
        }
    }
}
