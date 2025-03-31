package com.xianwei.customrpc.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian Serializer
 *
 * This class implements the Serializer interface using Hessian,
 * a binary web service protocol developed by Caucho.
 *
 * Hessian is compact, fast, and cross-language compatible,
 * making it a good choice for RPC frameworks.
 */
public class HessianSerializer implements Serializer {

    /**
     * Serialize an object into a byte array using Hessian.
     *
     * @param object the object to serialize
     * @param <T> the type of the object
     * @return the serialized byte array
     * @throws IOException if serialization fails
     */
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HessianOutput ho = new HessianOutput(bos);
        ho.writeObject(object);
        return bos.toByteArray();
    }

    /**
     * Deserialize a byte array into an object of the specified class using Hessian.
     *
     * @param bytes the serialized byte array
     * @param tClass the target class to deserialize into
     * @param <T> the type of the deserialized object
     * @return the deserialized object
     * @throws IOException if deserialization fails
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        HessianInput hi = new HessianInput(bis);
        return (T) hi.readObject(tClass);
    }
}

