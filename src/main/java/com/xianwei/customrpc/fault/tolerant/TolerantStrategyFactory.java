package com.xianwei.customrpc.fault.tolerant;

import com.xianwei.customrpc.spi.SpiLoader;

/**
 * Tolerant Strategy Factory
 *
 * Factory class used to obtain instances of fault-tolerance strategies using the SPI mechanism.
 */
public class TolerantStrategyFactory {

    // Static block: Load all TolerantStrategy implementations at application startup via SPI
    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    /**
     * Default tolerant strategy (used when no strategy is specified or lookup fails)
     */
    private static final TolerantStrategy DEFAULT_RETRY_STRATEGY = new FailFastTolerantStrategy();

    /**
     * Get an instance of a TolerantStrategy based on a given key
     *
     * @param key Identifier for the strategy (e.g., "failFast", "failSafe", "failOver")
     * @return Corresponding TolerantStrategy instance
     */
    public static TolerantStrategy getInstance(String key) {
        return SpiLoader.getInstance(TolerantStrategy.class, key);
    }
}

