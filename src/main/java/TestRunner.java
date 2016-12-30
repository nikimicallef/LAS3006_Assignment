import java.io.IOException;

public class TestRunner {
    public final static int PORT = 1927;
    public final static String ADDRESS = "localhost";
    public final static boolean DEBUG_MESSAGES = true;

    public static void main(String[] args) throws IOException {
        final MessageBrokerSelector messageBrokerSelector = new MessageBrokerSelector();
        final Server broker = new Server(messageBrokerSelector.getSelector(), PORT, ADDRESS);
        final Client client1 = new NewClient(PORT, ADDRESS);
        final Client client2 = new NewClient(PORT, ADDRESS);
        messageBrokerSelector.acceptConnections();
    }
}
