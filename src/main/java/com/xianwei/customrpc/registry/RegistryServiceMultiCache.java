package com.xianwei.customrpc.registry;

import com.xianwei.customrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local Registry Service Cache (Supports Multiple Services)
 *
 * This class maintains an in-memory cache for multiple services,
 * mapping each serviceKey to a list of available service instances (ServiceMetaInfo).
 */
public class RegistryServiceMultiCache {

    /**
     * Local cache mapping service keys to their respective list of service nodes.
     * ConcurrentHashMap ensures thread safety for concurrent access.
     */
    Map<String, List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();

    /**
     * Write (or update) the cache for a specific service key.
     *
     * @param serviceKey the unique key representing a service (e.g., "com.example.UserService:1.0")
     * @param newServiceCache the updated list of service instances
     */
    void writeCache(String serviceKey, List<ServiceMetaInfo> newServiceCache) {
        this.serviceCache.put(serviceKey, newServiceCache);
    }

    /**
     * Read the cached list of service instances for the given service key.
     *
     * @param serviceKey the key identifying the service
     * @return the list of service nodes, or null if not cached
     */
    List<ServiceMetaInfo> readCache(String serviceKey) {
        return this.serviceCache.get(serviceKey);
    }

    /**
     * Clear the cached entry for a specific service key.
     *
     * @param serviceKey the key of the service to remove from cache
     */
    void clearCache(String serviceKey) {
        this.serviceCache.remove(serviceKey);
    }
}
