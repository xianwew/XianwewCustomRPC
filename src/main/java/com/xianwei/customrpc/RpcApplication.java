package com.xianwei.customrpc;

import com.xianwei.customrpc.config.RegistryConfig;
import com.xianwei.customrpc.config.RpcConfig;
import com.xianwei.customrpc.constant.RpcConstant;
import com.xianwei.customrpc.registry.Registry;
import com.xianwei.customrpc.registry.RegistryFactory;
import com.xianwei.customrpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC Framework Application Context
 *
 * Acts as a global holder for the RPC configuration and manages lifecycle-related tasks.
 * Implements lazy-loaded singleton initialization using double-checked locking.
 */
@Slf4j
public class RpcApplication {

    /**
     * Global RPC configuration (singleton, thread-safe)
     */
    private static volatile RpcConfig rpcConfig;

    /**
     * Initialize the RPC framework with a custom configuration.
     *
     * @param newRpcConfig the configuration object to use
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("RPC initialized with config: {}", newRpcConfig.toString());

        // Initialize the registry based on the configuration
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("Registry initialized with config: {}", registryConfig);

        // Register a shutdown hook to gracefully destroy the registry on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    /**
     * Initialize the framework with default configuration.
     * Attempts to load from config files, or falls back to default values if not found.
     */
    public static void init() {
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            // Failed to load config, fall back to default config
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * Get the global RPC configuration.
     * If not initialized, initialize it lazily using default config.
     *
     * @return the loaded RpcConfig instance
     */
    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}

