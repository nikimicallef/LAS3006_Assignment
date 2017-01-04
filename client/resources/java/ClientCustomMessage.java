import java.io.Serializable;

/**
 * Created by niki on 03/01/17.
 */
public class ClientCustomMessage implements Serializable{
    private ClientMessageKey clientMessageKey = null;
    private CustomPath path = null;
    private String message = null;

    //Used for PUBLISH
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final CustomPath path, final String message) {
        this.clientMessageKey = clientMessageKey;
        this.path = path;
        this.message = message;
    }

    //Used for SUBSCRIBE
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final CustomPath path) {
        this.clientMessageKey = clientMessageKey;
        this.path = path;
    }

    //USED FOR PUBREC
    public ClientCustomMessage(ClientMessageKey clientMessageKey, String message) {
        this.clientMessageKey = clientMessageKey;
        this.message = message;
    }

    public ClientMessageKey getClientMessageKey() {
        return clientMessageKey;
    }

    public String getMessage() {
        return message;
    }

    public CustomPath getPath() {
        return path;
    }
}
