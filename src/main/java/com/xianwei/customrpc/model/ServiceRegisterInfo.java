package com.xianwei.customrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Service Registration Information
 *
 * This class holds the registration info for a service, including the name and the implementation class.
 *
 * @param <T> The type of the service interface
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegisterInfo<T> {

    /**
     * Name of the service (typically the interface or logical service name)
     */
    private String serviceName;

    /**
     * Implementation class of the service (must extend or implement T)
     */
    private Class<? extends T> implClass;
}
