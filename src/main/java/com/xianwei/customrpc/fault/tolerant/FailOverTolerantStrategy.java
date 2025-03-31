package com.xianwei.customrpc.fault.tolerant;

import com.xianwei.customrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Fail-Over Tolerant Strategy
 *
 * On failure, attempts to call the same service on a different service node.
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // TODO: Can be extended to retrieve another healthy service node and retry the call

        // Currently not implemented — returns null
        return null;
    }
}

