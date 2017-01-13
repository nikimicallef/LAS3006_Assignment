package resources;

import javax.management.MXBean;

@MXBean
public interface MessageGeneratorMbean {
    String getHardcodedPath();

    void setHardcodedPath(String hardcodedPath);
}
