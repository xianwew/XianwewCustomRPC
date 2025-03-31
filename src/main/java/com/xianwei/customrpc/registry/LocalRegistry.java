package com.xianwei.customrpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local Registry
 *
 * A simple in-memory registry for mapping service names to their implementation classes.
 * Used on the server side to dispatch incoming RPC calls to the correct implementation.
 */
public class LocalRegistry {

    /**
     * Storage for registered services.
     * Maps service names (typically fully qualified interface names) to implementation classes.
     */
    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * Register a service implementation class under a service name.
     *
     * @param serviceName the name of the service (usually interface's fully qualified name)
     * @param implClass the class implementing the service
     */
    public static void register(String serviceName, Class<?> implClass) {
        map.put(serviceName, implClass);
    }

    /**
     * Retrieve a registered service implementation class by its name.
     *
     * @param serviceName the name of the service
     * @return the registered implementation class, or null if not found
     */
    public static Class<?> get(String serviceName) {
        return map.get(serviceName);
    }

    /**
     * Remove a registered service from the registry.
     *
     * @param serviceName the name of the service to remove
     */
    public static void remove(String serviceName) {
        map.remove(serviceName);
    }
}

