package com.xianwei.customrpc.fault.retry;

import com.github.rholder.retry.*;
import com.xianwei.customrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Fixed Interval Retry Strategy
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    /**
     * Retry method with fixed interval
     *
     * @param callable The RPC call to be retried
     * @return The RPC response if successful
     * @throws ExecutionException If the task throws an exception
     * @throws RetryException If all retry attempts fail
     */
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws ExecutionException, RetryException {
        // Create a retryer that:
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // Retries if any Exception is thrown
                .retryIfExceptionOfType(Exception.class)
                // Waits 3 seconds between each retry
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                // Stops after 3 retry attempts
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // Logs each retry attempt
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("Retry attempt {}", attempt.getAttemptNumber());
                    }
                })
                .build();

        // Execute the callable with retry logic
        return retryer.call(callable);
    }

}

