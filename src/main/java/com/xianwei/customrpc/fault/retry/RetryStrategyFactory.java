package com.xianwei.customrpc.fault.retry;

import com.xianwei.customrpc.spi.SpiLoader;

/**
 * Retry Strategy Factory
 *
 * Used to obtain instances of different retry strategies via SPI (Service Provider Interface)
 */
public class RetryStrategyFactory {

    // Static block to load all RetryStrategy implementations at startup
    static {
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * Default retry strategy (used when none is specified)
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    /**
     * Get an instance of a retry strategy based on the given key
     *
     * @param key Identifier for the retry strategy (e.g., "fixed", "no", etc.)
     * @return The corresponding RetryStrategy implementation
     */
    public static RetryStrategy getInstance(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }

}

