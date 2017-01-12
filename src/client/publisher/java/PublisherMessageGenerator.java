import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PublisherMessageGenerator extends MessageGenerator {
    private final List<ClientMessageKey> messageTypes = Arrays.asList(ClientMessageKey.PUBLISH, ClientMessageKey.PINGREQ, ClientMessageKey.DISCONNECT);
    private String clientId;

    public PublisherMessageGenerator(final String clientId) {
        super();
        this.clientId = clientId;
    }

    @Override
    ClientCustomMessage generate() {
        final ClientMessageKey clientMessageKey = messageTypes.get(secureRandom.nextInt(messageTypes.size()));

        if(clientMessageKey == ClientMessageKey.PUBLISH){
            return new ClientCustomMessage(ClientMessageKey.PUBLISH, clientId, Long.toHexString(Double.doubleToLongBits(Math.random())), Paths.get(generatePublishPath()));
        } else if (clientMessageKey == ClientMessageKey.PINGREQ) {
            return new ClientCustomMessage(ClientMessageKey.PINGREQ, clientId, secureRandom.nextInt(9999));
        }
        if(secureRandom.nextInt(10) == 0) {
            return new ClientCustomMessage(ClientMessageKey.DISCONNECT);
        } else {
            return generate();
        }
    }
}
