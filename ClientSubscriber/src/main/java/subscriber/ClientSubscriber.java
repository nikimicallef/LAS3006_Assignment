package subscriber;

import properties.GlobalProperties;
import resources.*;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class ClientSubscriber extends Client {
    ClientSubscriber() {
        super();
    }

    public void read() throws IOException, ClassNotFoundException {
        final ByteBuffer buffer = ByteBuffer.allocate(4096);
        final ServerCustomMessage deserializedServerMessage;

        getClientSocketChannel().read(buffer);
        deserializedServerMessage = (ServerCustomMessage) GlobalProperties.deserializeMessage(buffer.array());


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
            System.out.println(deserializedServerMessage.getServerMessageKey() + " received. Unsubscribe status: " + deserializedServerMessage.getUnsubSuccessful());
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
            synchronized (getMessageGeneratorThreading().getMessageGenerator().getMessagesToWrite()) {
                getMessageGeneratorThreading().getMessageGenerator().getMessagesToWrite().add(new ClientCustomMessage(clientMessageKey, this.getClientId(), "Ack for " + dataReceived.getServerMessageKey().toString()));
            }
            getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        } else {
            System.out.println("Client is not connected yet and thus it can't write.");
        }
    }

    public static void main(String[] args) {
        final ClientSubscriber clientSubscriber = new ClientSubscriber();

        if (args.length > 0) {
            if (PathParsing.pathChecker(args[0])) {
                clientSubscriber.setMessageGenerator(new SubscriberMessageGenerator(clientSubscriber.getClientId(), args[0]));
                System.out.println("Client will be using path " + args[0]);
            } else {
                System.out.println("Inputted path is not valid. Starting without hardcoded path.");
                clientSubscriber.setMessageGenerator(new SubscriberMessageGenerator(clientSubscriber.getClientId()));
            }
        } else {
            clientSubscriber.setMessageGenerator(new SubscriberMessageGenerator(clientSubscriber.getClientId()));
            System.out.println("Client will be generating a path.");
        }

        try {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(clientSubscriber.getMessageGeneratorThreading().getMessageGenerator(), clientSubscriber.getMessageGeneratorThreading().getMessageGenerator().getObjectName());
            clientSubscriber.connectionManager();
        } catch (IOException | MalformedObjectNameException | NotCompliantMBeanException | InstanceAlreadyExistsException | MBeanRegistrationException e) {
            e.printStackTrace();
            clientSubscriber.getMessageGeneratorThreading().shutdown();
        }
    }
}
