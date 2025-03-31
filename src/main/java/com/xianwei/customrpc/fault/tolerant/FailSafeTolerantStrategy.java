package com.xianwei.customrpc.fault.tolerant;

import com.xianwei.customrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Fail-Safe Tolerant Strategy
 *
 * Silently handles exceptions without propagating them to the caller.
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // Log the exception without throwing it
        log.info("Silently handling exception", e);

        // Return an empty response to avoid breaking the caller
        return new RpcResponse();
    }
}
