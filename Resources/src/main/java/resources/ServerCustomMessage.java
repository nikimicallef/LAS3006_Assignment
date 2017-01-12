package resources;

import java.io.Serializable;

public class ServerCustomMessage implements Serializable {
    private final ServerMessageKey serverMessageKey;
    private final String message;
    private boolean unsubSuccessful = false;

    //Used for SUBACK
    public ServerCustomMessage(final ServerMessageKey serverMessageKey, final String message, final boolean unsubSuccessful) {
        this.serverMessageKey = serverMessageKey;
        this.unsubSuccessful = unsubSuccessful;
        this.message = message;
    }

    //Used for PUBLISH, PUBACK
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

    public boolean getUnsubSuccessful() {
        return unsubSuccessful;
    }
}
