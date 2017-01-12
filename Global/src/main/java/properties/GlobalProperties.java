package properties;

import java.io.*;

public class GlobalProperties {
    public static final String address = "localhost";
    public static final int port = 1927;

    public static byte[] serializeMessage(final Object customMessage) throws IOException {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             final ObjectOutput out = new ObjectOutputStream(byteArrayOutputStream)) {
            out.writeObject(customMessage);
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static Object deserializeMessage(final byte[] byteArray) throws IOException, ClassNotFoundException {
        try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
             final ObjectInput deserializedObject = new ObjectInputStream(byteArrayInputStream)) {
            return deserializedObject.readObject();
        }
    }
}
