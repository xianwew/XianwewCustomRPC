package com.xianwei.customrpc.serializer;

import com.xianwei.customrpc.spi.SpiLoader;

/**
 * Serializer Factory (Factory Pattern)
 *
 * This factory class is responsible for providing Serializer instances
 * based on a specified key (e.g., "json", "kryo", "hessian", "jdk").
 * It uses SPI (Service Provider Interface) to load implementations dynamically.
 */
public class SerializerFactory {

    // Load all Serializer implementations using SPI at class loading time
    static {
        SpiLoader.load(Serializer.class);
    }

    /**
     * Default serializer instance (fallback when no match is found)
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * Get a serializer instance by key.
     *
     * @param key the serializer name (e.g., "json", "kryo", "hessian", "jdk")
     * @return the corresponding serializer instance
     */
    public static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }
}
