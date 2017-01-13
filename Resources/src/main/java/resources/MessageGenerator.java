package resources;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class MessageGenerator implements Runnable, MessageGeneratorMbean {
    final int pollingRateSeconds = 1;
    public final List<String> topLevelPath = Arrays.asList("bedroom", "bathroom", "#", "+");
    public final List<String> midLevelPath = Arrays.asList("aircondition", "boiler", "#", "+");
    public final List<String> bottomLevelPath = Arrays.asList("temperature", "#", "+");
    public final SecureRandom secureRandom = new SecureRandom();
    final List<ClientCustomMessage> messagesToWrite = Collections.synchronizedList(new ArrayList<>());
    private String hardcodedPath = null;

    public MessageGenerator() {
    }

    public MessageGenerator(final String hardcodedPath) {
        this.hardcodedPath = hardcodedPath;
    }

    public int getPollingRateSeconds() {
        return pollingRateSeconds;
    }

    public List<ClientCustomMessage> getMessagesToWrite() {
        return messagesToWrite;
    }

    public abstract ClientCustomMessage generate();

    @Override
    public String getHardcodedPath() {
        return hardcodedPath;
    }

    @Override
    public void setHardcodedPath(String hardcodedPath) {
        this.hardcodedPath = hardcodedPath;
    }

    public ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("AssignmentMonitoring:type=MessageGenerator");
    }

    @Override
    public void run() {
        synchronized (messagesToWrite) {
            messagesToWrite.add(generate());
        }
    }
}
