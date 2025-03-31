package com.xianwei.customrpc.fault.retry;

import com.xianwei.customrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * No-Retry Strategy
 */
@Slf4j
public class NoRetryStrategy implements RetryStrategy {

    /**
     * Execute the RPC call without any retries.
     *
     * @param callable The RPC call to execute
     * @return The RPC response
     * @throws Exception If the call fails, the exception is propagated directly
     */
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        // Directly execute the callable without retrying
        return callable.call();
    }

}
