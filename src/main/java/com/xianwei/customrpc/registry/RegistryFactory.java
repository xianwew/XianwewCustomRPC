package com.xianwei.customrpc.registry;

import com.xianwei.customrpc.spi.SpiLoader;

/**
 * Registry Factory (used to obtain Registry implementations)
 *
 * This factory provides access to different implementations of the Registry interface,
 * using Java SPI (Service Provider Interface) for dynamic loading.
 */
public class RegistryFactory {

    // Load all Registry implementations using SPI at class load time
    static {
        SpiLoader.load(Registry.class);
    }

    /**
     * Default Registry implementation (fallback if SPI fails or key is invalid).
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * Get a Registry instance by key.
     * The key typically matches the identifier in the SPI configuration (e.g., "etcd").
     *
     * @param key the unique key representing a Registry implementation
     * @return an instance of the corresponding Registry
     */
    public static Registry getInstance(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }
}

