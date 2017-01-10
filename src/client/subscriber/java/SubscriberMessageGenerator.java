import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by niki on 09/01/17.
 */
public class SubscriberMessageGenerator extends MessageGenerator {
    private final List<ClientMessageKey> messageTypes = Arrays.asList(ClientMessageKey.SUBSCRIBE, ClientMessageKey.PINGREQ, ClientMessageKey.UNSUBSCRIBE, ClientMessageKey.DISCONNECT);

    public SubscriberMessageGenerator() {
        super();
    }

    @Override
    public ClientCustomMessage generate() {
        final ClientMessageKey clientMessageKey = messageTypes.get(secureRandom.nextInt(messageTypes.size()));

        if(clientMessageKey == ClientMessageKey.SUBSCRIBE){
            return new ClientCustomMessage(ClientMessageKey.SUBSCRIBE, Paths.get(generateSubscribePath()));
        } else if (clientMessageKey == ClientMessageKey.PINGREQ) {
            return new ClientCustomMessage(ClientMessageKey.PINGREQ, secureRandom.nextInt(9999));
        } else if (clientMessageKey == ClientMessageKey.UNSUBSCRIBE){
            return new ClientCustomMessage(ClientMessageKey.UNSUBSCRIBE, Paths.get(generateSubscribePath()));
        }
        if(secureRandom.nextInt(10) == 0) {
            return new ClientCustomMessage(ClientMessageKey.DISCONNECT);
        } else {
            return generate();
        }
    }
}
