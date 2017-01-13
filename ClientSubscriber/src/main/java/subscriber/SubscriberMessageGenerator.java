package subscriber;

import resources.ClientCustomMessage;
import resources.ClientMessageKey;
import resources.MessageGenerator;
import resources.PathParsing;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


public class SubscriberMessageGenerator extends MessageGenerator {
    private final List<ClientMessageKey> messageTypes = Arrays.asList(ClientMessageKey.SUBSCRIBE, ClientMessageKey.PINGREQ, ClientMessageKey.UNSUBSCRIBE, ClientMessageKey.DISCONNECT);
    private String clientId;

    public SubscriberMessageGenerator(final String clientId) {
        super();
        this.clientId = clientId;
    }

    public SubscriberMessageGenerator(final String clientId, final String path){
        super(path);
        this.clientId=clientId;
    }

    @Override
    public String generatePath() {
        String path = "";

        String levelPath = topLevelPath.get(secureRandom.nextInt(topLevelPath.size()));
        path += levelPath;

        if (levelPath.equals("#")) {
            return path;
        }

        path += "/";

        levelPath = midLevelPath.get(secureRandom.nextInt(midLevelPath.size()));
        path += levelPath;

        if (levelPath.equals("#")) {
            return path;
        }

        path += "/";

        levelPath = bottomLevelPath.get(secureRandom.nextInt(bottomLevelPath.size()));
        path += levelPath;

        return path;
    }

    @Override
    public ClientCustomMessage generateMessage() {
        final ClientMessageKey clientMessageKey = messageTypes.get(secureRandom.nextInt(messageTypes.size()));
        final String thisPath;

        if(getHardcodedPath() != null && PathParsing.pathChecker(getHardcodedPath())){
            thisPath = getHardcodedPath();
        } else {
            thisPath = generatePath();
        }

        if (clientMessageKey == ClientMessageKey.SUBSCRIBE) {
            return new ClientCustomMessage(ClientMessageKey.SUBSCRIBE, clientId, Paths.get(thisPath));
        } else if (clientMessageKey == ClientMessageKey.PINGREQ) {
            return new ClientCustomMessage(ClientMessageKey.PINGREQ, clientId, secureRandom.nextInt(9999));
        } else if (clientMessageKey == ClientMessageKey.UNSUBSCRIBE) {
            return new ClientCustomMessage(ClientMessageKey.UNSUBSCRIBE, clientId, Paths.get(thisPath));
        }
        if (secureRandom.nextInt(10) == 0) {
            return new ClientCustomMessage(ClientMessageKey.DISCONNECT);
        } else {
            return generateMessage();
        }
    }
}
