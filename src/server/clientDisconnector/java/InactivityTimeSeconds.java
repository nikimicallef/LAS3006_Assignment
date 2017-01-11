import javax.management.DynamicMBean;
import javax.management.MXBean;

/**
 * Created by niki on 11/01/17.
 */
@MXBean
public interface InactivityTimeSeconds {
    int getInactivityTime();

    void setInactivityTime(final int inactivityTime);
}
