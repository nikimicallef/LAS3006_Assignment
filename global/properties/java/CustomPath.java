import java.io.Serializable;

/**
 * Created by niki on 04/01/17.
 */
public class CustomPath implements Serializable {
    private String path;

    public CustomPath(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
