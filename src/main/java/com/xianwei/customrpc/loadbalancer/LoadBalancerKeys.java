package com.xianwei.customrpc.loadbalancer;

/**
 * Load Balancer Key Constants
 *
 * Used as identifiers for different load balancing strategies in the factory or config.
 */
public interface LoadBalancerKeys {

    /**
     * Round-robin load balancing
     */
    String ROUND_ROBIN = "roundRobin";

    /**
     * Random load balancing
     */
    String RANDOM = "random";

    /**
     * Consistent hashing load balancing
     */
    String CONSISTENT_HASH = "consistentHash";
}

