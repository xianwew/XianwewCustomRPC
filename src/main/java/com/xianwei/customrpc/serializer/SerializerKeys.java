package com.xianwei.customrpc.serializer;

/**
 * Serializer Key Constants
 *
 * This interface defines constant keys used to identify different Serializer implementations.
 * These keys are typically used with the SerializerFactory to select a serializer.
 */
public interface SerializerKeys {

    /**
     * Key for Java built-in serialization
     */
    String JDK = "jdk";

    /**
     * Key for JSON serialization (e.g., using Jackson)
     */
    String JSON = "json";

    /**
     * Key for Kryo serialization (fast, binary format)
     */
    String KRYO = "kryo";

    /**
     * Key for Hessian serialization (binary, cross-language)
     */
    String HESSIAN = "hessian";
}

