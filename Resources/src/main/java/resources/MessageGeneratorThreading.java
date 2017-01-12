package resources;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MessageGeneratorThreading {
    private final ScheduledExecutorService scheduledExecutorService;
    private final MessageGenerator messageGenerator;

    public MessageGeneratorThreading(final MessageGenerator messageGenerator) {
        this.messageGenerator = messageGenerator;
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.scheduleWithFixedDelay(messageGenerator, 0, messageGenerator.getPollingRateSeconds(), TimeUnit.SECONDS);
    }

    public MessageGenerator getMessageGenerator() {
        return messageGenerator;
    }

    public void shutdown() {
        scheduledExecutorService.shutdownNow();
    }
}
