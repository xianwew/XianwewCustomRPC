package com.xianwei.customrpc.proxy;

import com.xianwei.customrpc.RpcApplication;

import java.lang.reflect.Proxy;

/**
 * Service Proxy Factory (Factory Pattern)
 *
 * This factory class is responsible for creating proxy instances for RPC service interfaces.
 * It supports both real and mock service proxies using Java's dynamic proxy mechanism.
 */
public class ServiceProxyFactory {

    /**
     * Get a proxy instance for the given service interface.
     * This will either return a real RPC proxy or a mock proxy based on configuration.
     *
     * @param serviceClass the interface class representing the remote service
     * @param <T> the type of the service
     * @return a proxy that implements the service interface
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        // If mock mode is enabled, return a mock proxy
        if (RpcApplication.getRpcConfig().isMock()) {
            return getMockProxy(serviceClass);
        }

        // Create and return a dynamic proxy that delegates to ServiceProxy
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy()
        );
    }

    /**
     * Get a mock proxy for the given service interface.
     * This is used for testing or fallback when RPC is disabled.
     *
     * @param serviceClass the interface class representing the remote service
     * @param <T> the type of the service
     * @return a mock proxy that implements the service interface
     */
    public static <T> T getMockProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy()
        );
    }
}
