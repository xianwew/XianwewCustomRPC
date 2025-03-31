package com.xianwei.customrpc.config;

import com.xianwei.customrpc.registry.RegistryKeys;
import lombok.Data;

/**
 * RPC Framework Registry Center Configuration
 */
@Data
public class RegistryConfig {

    /**
     * Type of the registry center (e.g., ETCD, Zookeeper, Nacos, etc.)
     */
    private String registry = RegistryKeys.ETCD;

    /**
     * Address of the registry center (e.g., URL or IP + port)
     */
    private String address = "http://localhost:2380";

    /**
     * Username for accessing the registry (if authentication is required)
     */
    private String username;

    /**
     * Password for accessing the registry (if authentication is required)
     */
    private String password;

    /**
     * Timeout duration in milliseconds
     */
    private Long timeout = 10000L;
}

