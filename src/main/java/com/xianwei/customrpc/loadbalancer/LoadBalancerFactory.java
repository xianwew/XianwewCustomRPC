package com.xianwei.customrpc.loadbalancer;

import com.xianwei.customrpc.spi.SpiLoader;

/**
 * Load Balancer Factory
 *
 * Factory class used to obtain LoadBalancer instances via SPI (Service Provider Interface).
 */
public class LoadBalancerFactory {

    // Load all LoadBalancer implementations at startup via SPI
    static {
        SpiLoader.load(LoadBalancer.class);
    }

    /**
     * Default load balancer (used when none is specified)
     */
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();

    /**
     * Get a LoadBalancer instance based on the given key
     *
     * @param key Identifier of the desired load balancing strategy (e.g., "roundRobin", "random", "consistentHash")
     * @return Corresponding LoadBalancer implementation
     */
    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }

}

