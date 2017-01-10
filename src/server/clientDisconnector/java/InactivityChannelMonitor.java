import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by niki on 10/01/17.
 */
public class InactivityChannelMonitor implements Runnable {
    private final Map<SelectionKey, Long> lastSelectionKeyActivityTime = new ConcurrentHashMap<>();
    final int inactivityTimeSeconds = 5;
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

    public synchronized List<SelectionKey> getKeysToInvalidate() {
        return keysToInvalidate;
    }

    public void setKeysToInvalidate(final List<SelectionKey> keysToInvalidate) {
        this.keysToInvalidate = keysToInvalidate;
    }

    public synchronized Map<SelectionKey, Long> getLastSelectionKeyActivityTime() {
        return lastSelectionKeyActivityTime;
    }



    @Override
    public void run() {
        setInactiveChannels();
    }
}
