import javax.management.MXBean;
import java.util.Map;
import java.util.Set;

/**
 * Created by niki on 11/01/17.
 */
@MXBean
public interface ServerMbean {
    int getClientsConnected();

    Set<String> getActivePath();

    int getTotalMessagesDelivered();

    Map<String, Integer> getNoOfMessagesDeliveredPerTopic();

    Map<String, Integer> getNoOfMessagesDeliveredToEachClient();

    Map<String, Integer> noOfMessagesPublishedByEachClient();
}
