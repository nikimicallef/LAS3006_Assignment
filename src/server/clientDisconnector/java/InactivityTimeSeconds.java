import javax.management.DynamicMBean;
import javax.management.MXBean;

@MXBean
public interface InactivityTimeSeconds {
    int getInactivityTime();

    void setInactivityTime(final int inactivityTime);
}
