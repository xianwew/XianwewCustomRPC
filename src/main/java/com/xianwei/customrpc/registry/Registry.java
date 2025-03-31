package com.xianwei.customrpc.registry;

import com.xianwei.customrpc.config.RegistryConfig;
import com.xianwei.customrpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * Registry
 *
 * This interface defines the core operations for a service registry in an RPC framework.
 * It supports service registration, discovery, heartbeat renewal, and change watching.
 */
public interface Registry {

    /**
     * Initialize the registry with the provided configuration.
     *
     * @param registryConfig configuration for connecting to the registry (e.g., etcd address, timeout)
     */
    void init(RegistryConfig registryConfig);

    /**
     * Register a service instance (typically called by the server).
     *
     * @param serviceMetaInfo metadata describing the service (name, host, port, version)
     * @throws Exception if registration fails
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * Unregister a service instance (typically called when the server is shutting down).
     *
     * @param serviceMetaInfo metadata for the service to remove
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo);

    /**
     * Discover all instances of a given service (used by clients).
     *
     * @param serviceKey unique key for the service (usually name + version)
     * @return list of service metadata representing available providers
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     * Start a scheduled heartbeat for lease renewal (used by servers).
     * Keeps the service alive in the registry.
     */
    void heartBeat();

    /**
     * Watch for changes (e.g. deletion) on a specific service node key (used by clients).
     *
     * @param serviceNodeKey the full key representing a service instance (e.g. `UserService:1.0/localhost:8080`)
     */
    void watch(String serviceNodeKey);

    /**
     * Destroy the registry and clean up resources (e.g., remove registered services).
     * Called when the application is shutting down.
     */
    void destroy();
}

