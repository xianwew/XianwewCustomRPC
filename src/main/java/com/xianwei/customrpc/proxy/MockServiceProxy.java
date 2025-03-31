package com.xianwei.customrpc.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mock Service Proxy (using JDK Dynamic Proxy)
 *
 * This class acts as a mock implementation of any service interface.
 * It's primarily used for testing or simulating remote service behavior.
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {

    /**
     * Method called when a method is invoked on the proxy instance.
     *
     * @param proxy  the proxy instance
     * @param method the method being called
     * @param args   the arguments to the method
     * @return a default/mock return value for the method
     * @throws Throwable if an error occurs during invocation
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Get the return type of the method
        Class<?> methodReturnType = method.getReturnType();

        // Log the method being mocked
        log.info("Mock invoke {}", method.getName());

        // Return a default value based on the return type
        return getDefaultObject(methodReturnType);
    }

    /**
     * Generate a default/mock object for a given return type.
     * Extend this method to simulate more realistic mock behavior.
     *
     * @param type the class type of the return value
     * @return a default object corresponding to the type
     */
    private Object getDefaultObject(Class<?> type) {
        // For primitive return types, return a simple default
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                return false;
            } else if (type == short.class) {
                return (short) 0;
            } else if (type == int.class) {
                return 0;
            } else if (type == long.class) {
                return 0L;
            }
        }

        // For non-primitives (objects), return null by default
        return null;
    }
}
