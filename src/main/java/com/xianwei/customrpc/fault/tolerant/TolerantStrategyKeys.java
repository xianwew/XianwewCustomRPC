package com.xianwei.customrpc.fault.tolerant;

/**
 * Tolerant Strategy Key Constants
 *
 * Used to identify specific tolerant strategy implementations.
 */
public interface TolerantStrategyKeys {

    /**
     * Fail-Back: Degrade gracefully to a fallback service
     */
    String FAIL_BACK = "failBack";

    /**
     * Fail-Fast: Immediately throw an exception on failure
     */
    String FAIL_FAST = "failFast";

    /**
     * Fail-Over: Retry the request on a different service node
     */
    String FAIL_OVER = "failOver";

    /**
     * Fail-Safe: Silently handle the failure and return a default response
     */
    String FAIL_SAFE = "failSafe";
}

