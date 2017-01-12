package resources;

import java.io.Serializable;

public enum ClientMessageKey implements Serializable {
    CONNECT,
    SUBSCRIBE,
    PINGREQ,
    PUBLISH,
    PUBREC,
    UNSUBSCRIBE,
    DISCONNECT
}
