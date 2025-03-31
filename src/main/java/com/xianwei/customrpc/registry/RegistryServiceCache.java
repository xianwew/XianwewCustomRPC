package com.xianwei.customrpc.registry;

import com.xianwei.customrpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * Local Cache for Registry Services
 *
 * This class provides a simple in-memory cache for service discovery results.
 * It is designed for caching a **single service key** (deprecated in favor of multi-service caching).
 */
public class RegistryServiceCache {

    /**
     * Cached list of service metadata (only supports one service at a time).
     */
    List<ServiceMetaInfo> serviceCache;

    /**
     * Write to the cache.
     *
     * @param newServiceCache the list of service metadata to store
     */
    void writeCache(List<ServiceMetaInfo> newServiceCache) {
        this.serviceCache = newServiceCache;
    }

    /**
     * Read from the cache.
     *
     * @return the cached list of service metadata, or null if not cached
     */
    List<ServiceMetaInfo> readCache() {
        return this.serviceCache;
    }

    /**
     * Clear the cached data.
     */
    void clearCache() {
        this.serviceCache = null;
    }
}
