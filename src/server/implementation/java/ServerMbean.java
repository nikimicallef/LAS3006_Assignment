import javax.management.MXBean;
import java.util.Map;
import java.util.Set;

@MXBean
public interface ServerMbean {
    int getClientsConnected();

    Set<String> getActivePath();

    int getTotalMessagesDelivered();

    Map<String, Integer> getNoOfMessagesDeliveredPerTopic();

    Map<String, Integer> getNoOfMessagesDeliveredToEachClient();

    Map<String, Integer> noOfMessagesPublishedByEachClient();
}
