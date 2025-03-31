package com.xianwei.customrpc.fault.tolerant;

import com.xianwei.customrpc.model.RpcResponse;

import java.util.Map;

/**
 * Fail-Fast Tolerant Strategy
 *
 * Immediately fails and notifies the caller upon encountering an error.
 */
public class FailFastTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // Immediately throw an exception — no retry, no fallback
        throw new RuntimeException("Service error occurred", e);
    }
}

