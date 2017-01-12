package resources;

import java.io.Serializable;
import java.nio.file.Path;

public class ClientCustomMessage implements Serializable {
    private ClientMessageKey clientMessageKey = null;
    private String message = null;
    private String path = null;
    private String clientId;
    private int messageId;

    //USED FOR PUBLISH
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final String clientId, final String message, final Path path) {
        this.clientMessageKey = clientMessageKey;
        this.clientId = clientId;
        this.message = message;
        this.path = path.toString();
    }

    //USED FOR PUBREC
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final String clientId, final String message) {
        this.clientMessageKey = clientMessageKey;
        this.clientId = clientId;
        this.message = message;
    }

    //USED FOR SUBSCRIBE, UNSUBSCRIBE
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final String clientId, final Path path) {
        this.clientMessageKey = clientMessageKey;
        this.clientId = clientId;
        this.path = path.toString();
    }

    //USED FOR CONNECT
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final String clientId) {
        this.clientMessageKey = clientMessageKey;
        this.clientId = clientId;
    }

    //USED FOR PING
    public ClientCustomMessage(final ClientMessageKey clientMessageKey, final String clientId, final int messageId) {
        this.clientMessageKey = clientMessageKey;
        this.messageId = messageId;
    }

    //USED FOR DISCONNECT
    public ClientCustomMessage(final ClientMessageKey clientMessageKey) {
        this.clientMessageKey = clientMessageKey;
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

    public String getClientId() {
        return clientId;
    }

    public int getMessageId() {
        return messageId;
    }
}
