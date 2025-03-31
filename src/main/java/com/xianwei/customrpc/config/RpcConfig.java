package com.xianwei.customrpc.config;

import com.xianwei.customrpc.fault.retry.RetryStrategyKeys;
import com.xianwei.customrpc.fault.tolerant.TolerantStrategyKeys;
import com.xianwei.customrpc.loadbalancer.LoadBalancerKeys;
import com.xianwei.customrpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * Global Configuration for the RPC Framework
 */
@Data
public class RpcConfig {

    /**
     * Application name (used to identify the service/application)
     */
    private String name = "xianwei-custom-rpc";

    /**
     * Version of the RPC framework or the application
     */
    private String version = "1.0";

    /**
     * Hostname or IP address of the server
     */
    private String serverHost = "localhost";

    /**
     * Port on which the server will listen for requests
     */
    private Integer serverPort = 8080;

    /**
     * Serializer type to use (e.g., JDK, JSON, Kryo, etc.)
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * Load balancing strategy (e.g., Round-Robin, Random, Consistent Hashing)
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * Retry strategy (e.g., No Retry, Fixed Interval, Exponential Backoff)
     */
    private String retryStrategy = RetryStrategyKeys.NO;

    /**
     * Fault tolerance strategy (e.g., Fail Fast, Fail Over, Fail Safe)
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;

    /**
     * Whether to enable mock invocation (used for testing or fallback)
     */
    private boolean mock = false;

    /**
     * Configuration for the service registry (e.g., Etcd, Zookeeper)
     */
    private RegistryConfig registryConfig = new RegistryConfig();
}
