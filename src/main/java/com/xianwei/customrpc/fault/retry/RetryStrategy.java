package com.xianwei.customrpc.fault.retry;


import com.xianwei.customrpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * Retry Strategy Interface
 *
 * Defines the contract for implementing different retry mechanisms.
 */
public interface RetryStrategy {

    /**
     * Retry method to be implemented by concrete strategies.
     *
     * @param callable The RPC call to execute (possibly with retries)
     * @return RpcResponse if the call succeeds
     * @throws Exception If the call fails (after all retry attempts, if any)
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}

