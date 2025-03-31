package com.xianwei.customrpc.model;

import com.xianwei.customrpc.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC Request
 *
 * This class represents a request to be sent in a Remote Procedure Call (RPC) framework.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    /**
     * Name of the service being called
     */
    private String serviceName;

    /**
     * Name of the method being invoked
     */
    private String methodName;

    /**
     * Version of the service (defaults to a constant value)
     */
    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * Array of parameter types (used for method identification via reflection)
     */
    private Class<?>[] parameterTypes;

    /**
     * Array of arguments to be passed to the method
     */
    private Object[] args;

}

