import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by niki on 09/01/17.
 */
public abstract class MessageGenerator implements Runnable {
    final int waitTimeSeconds = 5;
    final List<String> topLevelPath = Arrays.asList("bedroom", "bathroom", "kitchen", "#", "+");
    final List<String> midLevelPath = Arrays.asList("aircondition", "boiler", "heater", "#", "+");
    final List<String> bottomLevelPath = Arrays.asList("temperature", "status", "#", "+");
    final SecureRandom secureRandom = new SecureRandom();
    final List<ClientCustomMessage> messagesToWrite = Collections.synchronizedList(new ArrayList<>());

    public int getWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    public List<ClientCustomMessage> getMessagesToWrite() {
        return messagesToWrite;
    }

    abstract ClientCustomMessage generate();

    String generateSubscribePath() {
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

    String generatePublishPath() {
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
    public void run() {
        messagesToWrite.add(generate());
    }
}
