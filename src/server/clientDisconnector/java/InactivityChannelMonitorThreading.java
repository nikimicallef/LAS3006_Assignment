import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by niki on 10/01/17.
 */
public class InactivityChannelMonitorThreading {
    private final ScheduledExecutorService scheduledExecutorService;
    private final InactivityChannelMonitor inactivityChannelMonitor;

    public InactivityChannelMonitorThreading(final InactivityChannelMonitor inactivityChannelMonitor) {
        this.inactivityChannelMonitor = inactivityChannelMonitor;
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.scheduleWithFixedDelay(inactivityChannelMonitor,
                0,
                inactivityChannelMonitor.getPollingRateSeconds(),
                TimeUnit.SECONDS);
    }

    public synchronized InactivityChannelMonitor getInactivityChannelMonitor() {
        return inactivityChannelMonitor;
    }

    public void shutdown() {
        scheduledExecutorService.shutdownNow();
    }
}
