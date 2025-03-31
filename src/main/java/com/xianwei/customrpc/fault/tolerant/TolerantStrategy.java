package com.xianwei.customrpc.fault.tolerant;



import com.xianwei.customrpc.model.RpcResponse;

import java.util.Map;

/**
 * Tolerant Strategy Interface
 *
 * Defines how the RPC framework should handle faults or exceptions.
 */
public interface TolerantStrategy {

    /**
     * Fault-tolerant handling method
     *
     * @param context Context data (can include request info, metadata, etc.)
     * @param e       The exception that occurred
     * @return A fallback or default RpcResponse
     */
    RpcResponse doTolerant(Map<String, Object> context, Exception e);
}

