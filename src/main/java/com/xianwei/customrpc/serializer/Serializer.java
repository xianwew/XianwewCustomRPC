package com.xianwei.customrpc.serializer;

import java.io.IOException;

/**
 * Serializer Interface
 *
 * Defines a standard interface for serialization and deserialization,
 * allowing different implementations (e.g., JDK, JSON, Kryo, Hessian) to be used interchangeably.
 */
public interface Serializer {

    /**
     * Serialize an object to a byte array.
     *
     * @param object the object to serialize
     * @param <T> the type of the object
     * @return the serialized byte array
     * @throws IOException if serialization fails
     */
    <T> byte[] serialize(T object) throws IOException;

    /**
     * Deserialize a byte array back into an object of the specified class type.
     *
     * @param bytes the serialized byte array
     * @param tClass the target class for deserialization
     * @param <T> the type of the resulting object
     * @return the deserialized object
     * @throws IOException if deserialization fails
     */
    <T> T deserialize(byte[] bytes, Class<T> tClass) throws IOException;
}

