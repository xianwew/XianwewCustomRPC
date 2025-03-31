package com.xianwei.customrpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo Serializer
 *
 * Implements the Serializer interface using Kryo, a fast and efficient binary serialization library.
 * Since Kryo is not thread-safe, ThreadLocal is used to ensure each thread has its own Kryo instance.
 */
public class KryoSerializer implements Serializer {

    /**
     * Kryo is not thread-safe. Use ThreadLocal to give each thread its own Kryo instance.
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // Allow serialization without requiring prior class registration.
        // Note: this may cause security issues in untrusted environments.
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    /**
     * Serialize an object into a byte array using Kryo.
     *
     * @param obj the object to serialize
     * @param <T> the type of the object
     * @return serialized byte array
     */
    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        KRYO_THREAD_LOCAL.get().writeObject(output, obj);
        output.close(); // closes the stream and flushes it
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Deserialize a byte array into an object of the specified class using Kryo.
     *
     * @param bytes the byte array to deserialize
     * @param classType the expected class type of the result
     * @param <T> the type of the result
     * @return the deserialized object
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> classType) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream);
        T result = KRYO_THREAD_LOCAL.get().readObject(input, classType);
        input.close(); // close input stream
        return result;
    }
}

