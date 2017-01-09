import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by niki on 09/01/17.
 */
public class PublisherMessageGenerator extends MessageGenerator {
    private final List<ClientMessageKey> messageTypes = Arrays.asList(ClientMessageKey.PUBLISH, ClientMessageKey.PINGREQ, ClientMessageKey.DISCONNECT);

    public PublisherMessageGenerator() {
        super();
    }

    @Override
    ClientCustomMessage generate() {
        final ClientMessageKey clientMessageKey = messageTypes.get(secureRandom.nextInt(messageTypes.size()));

        if(clientMessageKey == ClientMessageKey.PUBLISH){
            return new ClientCustomMessage(ClientMessageKey.PUBLISH, Long.toHexString(Double.doubleToLongBits(Math.random())), Paths.get(generatePublishPath()));
        } else if (clientMessageKey == ClientMessageKey.PINGREQ) {
            return new ClientCustomMessage(ClientMessageKey.PINGREQ, secureRandom.nextInt(9999));
        }
        return new ClientCustomMessage(ClientMessageKey.DISCONNECT);
    }
}
