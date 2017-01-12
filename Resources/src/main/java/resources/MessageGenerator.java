package resources;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class MessageGenerator implements Runnable {
    final int pollingRateSeconds = 1;
    final List<String> topLevelPath = Arrays.asList("bedroom", "bathroom", "#", "+");
    final List<String> midLevelPath = Arrays.asList("aircondition", "boiler", "#", "+");
    final List<String> bottomLevelPath = Arrays.asList("temperature", "#", "+");
    public final SecureRandom secureRandom = new SecureRandom();
    final List<ClientCustomMessage> messagesToWrite = Collections.synchronizedList(new ArrayList<>());

    public int getPollingRateSeconds() {
        return pollingRateSeconds;
    }

    public List<ClientCustomMessage> getMessagesToWrite() {
        return messagesToWrite;
    }

    public abstract ClientCustomMessage generate();

    public String generateSubscribePath() {
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

    public String generatePublishPath() {
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
        synchronized (messagesToWrite) {
            messagesToWrite.add(generate());
        }
    }
}
