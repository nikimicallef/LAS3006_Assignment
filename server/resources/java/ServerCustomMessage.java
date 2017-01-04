import java.io.Serializable;

/**
 * Created by niki on 03/01/17.
 */
public class ServerCustomMessage implements Serializable{
    private final ServerMessageKey serverMessageKey;
    private final String message;

    //Used for SUBACK, PUBLISH, PUBACK
    public ServerCustomMessage(final ServerMessageKey serverMessageKey, final String message) {
        this.serverMessageKey = serverMessageKey;
        this.message = message;
    }

    public ServerMessageKey getServerMessageKey() {
        return serverMessageKey;
    }

    public String getMessage() {
        return message;
    }
}
