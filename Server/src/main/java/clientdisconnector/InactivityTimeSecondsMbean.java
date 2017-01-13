package clientdisconnector;

import javax.management.MXBean;

@MXBean
public interface InactivityTimeSecondsMbean {
    int getInactivityTime();

    void setInactivityTime(final int inactivityTime);
}
