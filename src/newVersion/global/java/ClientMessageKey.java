import java.io.Serializable;

/**
 * Created by niki on 03/01/17.
 */
public enum ClientMessageKey implements Serializable {
    CONNECT,
    SUBSCRIBE,
    PINGREQ,
    PUBLISH,
    PUBREC,
    UNSUBSCRIBE,
    DISCONNECT
}
