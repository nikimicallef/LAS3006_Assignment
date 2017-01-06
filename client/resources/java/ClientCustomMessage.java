import java.io.Serializable;
import java.nio.file.Path;

/**
 * Created by niki on 03/01/17.
 */
public class ClientCustomMessage implements Serializable{
    private ClientMessageKey clientMessageKey = null;
    private String message = null;
    private String path = null;
    private long clientId;

    //USED FOR PUBLISH
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final String message, final Path path) {
        this.clientMessageKey = clientMessageKey;
        this.message = message;
        this.path = path.toString();
    }

    //USED FOR PUBREC
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final String message) {
        this.clientMessageKey = clientMessageKey;
        this.message = message;
    }

    //USED FOR SUBSCRIBE
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final Path path) {
        this.clientMessageKey = clientMessageKey;
        this.path = path.toString();
    }

    //USED FOR CONNECT
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final long clientId) {
        this.clientMessageKey = clientMessageKey;
        this.clientId = clientId;
    }

    public ClientMessageKey getClientMessageKey() {
        return clientMessageKey;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public long getClientId() {
        return clientId;
    }
}
