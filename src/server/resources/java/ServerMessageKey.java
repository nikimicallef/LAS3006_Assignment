import java.io.Serializable;

public enum ServerMessageKey implements Serializable {
    CONNACK,
    SUBACK,
    PINGRESP,
    PUBACK,
    PUBLISH,
    UNSUBACK
}
