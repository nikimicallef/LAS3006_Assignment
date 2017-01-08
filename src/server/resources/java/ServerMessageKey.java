import java.io.Serializable;

/**
 * Created by niki on 03/01/17.
 */
public enum ServerMessageKey implements Serializable {
    CONNACK,
    SUBACK,
    PINGRESP,
    PUBACK,
    PUBLISH,
    UNSUBACK
}
