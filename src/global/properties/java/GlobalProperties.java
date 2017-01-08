import java.io.*;

/**
 * Created by niki on 30/12/16.
 */
public class GlobalProperties {
    static final String address = "localhost";
    static final int port = 1927;

    static byte[] serializeMessage(final Object customMessage) throws IOException {
        try (final ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
             final ObjectOutput out = new ObjectOutputStream(byteArrayOutputStream)) {
            out.writeObject(customMessage);
            return byteArrayOutputStream.toByteArray();
        }
    }

    static Object deserializeMessage(final byte[] byteArray) throws IOException, ClassNotFoundException {
        try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
             final ObjectInput deserializedObject = new ObjectInputStream(byteArrayInputStream)) {
            return deserializedObject.readObject();
        }
    }
}
