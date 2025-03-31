package com.xianwei.customrpc.serializer;

import java.io.*;

/**
 * JDK Serializer
 *
 * This class implements the Serializer interface using Java's built-in serialization mechanism.
 * It requires all serialized classes to implement the Serializable interface.
 */
public class JdkSerializer implements Serializer {

    /**
     * Serialize an object into a byte array using Java's built-in ObjectOutputStream.
     *
     * @param object the object to serialize
     * @param <T> the type of the object
     * @return the serialized byte array
     * @throws IOException if an I/O error occurs
     */
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close(); // flush & close stream
        return outputStream.toByteArray();
    }

    /**
     * Deserialize a byte array back into an object using ObjectInputStream.
     *
     * @param bytes the serialized byte array
     * @param type the target class type
     * @param <T> the type of the returned object
     * @return the deserialized object
     * @throws IOException if an I/O error occurs
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        try {
            return (T) objectInputStream.readObject(); // unsafe cast — user must ensure type safety
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e); // wrap checked exception
        } finally {
            objectInputStream.close(); // clean up resources
        }
    }
}

