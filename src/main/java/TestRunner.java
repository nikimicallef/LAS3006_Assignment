import java.io.IOException;

public class TestRunner {
    public final static int PORT = 1927;
    public final static String ADDRESS = "localhost";
    public final static boolean DEBUG_MESSAGES = true;

    public static void main(String[] args) throws IOException {
        final MessageBrokerSelector messageBrokerSelector = new MessageBrokerSelector();
        final Broker broker = new Broker(messageBrokerSelector.getSelector(), PORT, ADDRESS);
        final Client client1 = new Client(PORT, ADDRESS);
        final Client client2 = new Client(PORT, ADDRESS);
        messageBrokerSelector.acceptConnections();
    }
}
