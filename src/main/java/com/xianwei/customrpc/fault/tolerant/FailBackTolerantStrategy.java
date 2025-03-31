package com.xianwei.customrpc.fault.tolerant;

import com.xianwei.customrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Fail-Back Tolerant Strategy
 *
 * Falls back to a backup service or alternative logic in case of failure.
 */
@Slf4j
public class FailBackTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // TODO: Can be extended to retrieve and invoke a fallback service

        // Currently returns null — no fallback implemented yet
        return null;
    }
}

