package publisher;

import resources.ClientCustomMessage;
import resources.ClientMessageKey;
import resources.MessageGeneratorMbean;
import resources.PathParsing;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PublisherMessageGenerator extends resources.MessageGenerator implements MessageGeneratorMbean {
    private final List<ClientMessageKey> messageTypes = Arrays.asList(ClientMessageKey.PUBLISH, ClientMessageKey.PINGREQ, ClientMessageKey.DISCONNECT);
    private String clientId;

    public PublisherMessageGenerator(final String clientId) {
        super();
        this.clientId = clientId;
    }

    public PublisherMessageGenerator(final String clientId, final String hardcodedPath) {
        super(hardcodedPath);
        this.clientId = clientId;
    }

    @Override
    public String generatePath() {
        String path = "";
        final int pathMaxLength = secureRandom.nextInt(3) + 1;

        String levelPath = topLevelPath.get(secureRandom.nextInt(topLevelPath.size()));

        while (levelPath.equals("#") || levelPath.equals("+")) {
            levelPath = topLevelPath.get(secureRandom.nextInt(topLevelPath.size()));
        }

        path += levelPath;

        if (pathMaxLength == 1) {
            return path;
        }

        path += "/";

        levelPath = midLevelPath.get(secureRandom.nextInt(midLevelPath.size()));

        while (levelPath.equals("#") || levelPath.equals("+")) {
            levelPath = topLevelPath.get(secureRandom.nextInt(topLevelPath.size()));
        }

        path += levelPath;

        if (pathMaxLength == 2) {
            return path;
        }

        path += "/";

        levelPath = bottomLevelPath.get(secureRandom.nextInt(bottomLevelPath.size()));

        while (levelPath.equals("#") || levelPath.equals("+")) {
            levelPath = topLevelPath.get(secureRandom.nextInt(topLevelPath.size()));
        }

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

        if (clientMessageKey == ClientMessageKey.PUBLISH) {
            return new ClientCustomMessage(ClientMessageKey.PUBLISH, clientId, Long.toHexString(Double.doubleToLongBits(Math.random())), Paths.get(thisPath));
        } else if (clientMessageKey == ClientMessageKey.PINGREQ) {
            return new ClientCustomMessage(ClientMessageKey.PINGREQ, clientId, secureRandom.nextInt(9999));
        }
        if (secureRandom.nextInt(10) == 0) {
            return new ClientCustomMessage(ClientMessageKey.DISCONNECT);
        } else {
            return generateMessage();
        }
    }
}
