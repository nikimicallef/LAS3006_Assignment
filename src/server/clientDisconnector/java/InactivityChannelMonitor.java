import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InactivityChannelMonitor implements Runnable, InactivityTimeSeconds {
    private final Map<SelectionKey, Long> lastSelectionKeyActivityTime = new ConcurrentHashMap<>();
    volatile int inactivityTimeSeconds = 5;
    private List<SelectionKey> keysToInvalidate = Collections.synchronizedList(new ArrayList<>());
    private final int pollingRateSeconds = 1;

    private void setInactiveChannels() {
        synchronized (getKeysToInvalidate()) {
            lastSelectionKeyActivityTime.keySet().stream().filter(selectionKey -> System.currentTimeMillis() - lastSelectionKeyActivityTime.get(selectionKey) > getInactivityTimeMilliseconds()).forEach(keysToInvalidate::add);
        }
    }

    private Long getInactivityTimeMilliseconds() {
        return (long) inactivityTimeSeconds * 1000;
    }

    public int getPollingRateSeconds() {
        return pollingRateSeconds;
    }

    public List<SelectionKey> getKeysToInvalidate() {
        return keysToInvalidate;
    }

    public void setKeysToInvalidate(final List<SelectionKey> keysToInvalidate) {
        this.keysToInvalidate = keysToInvalidate;
    }

    public Map<SelectionKey, Long> getLastSelectionKeyActivityTime() {
        return lastSelectionKeyActivityTime;
    }

    @Override
    public void run() {
        setInactiveChannels();
    }

    @Override
    public int getInactivityTime() {
        return inactivityTimeSeconds;
    }

    @Override
    public void setInactivityTime(final int inactivityTime){
        this.inactivityTimeSeconds = inactivityTime;
    }

    public ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("AssignmentMonitoring:type=InactivityChannelMonitoring");
    }
}
