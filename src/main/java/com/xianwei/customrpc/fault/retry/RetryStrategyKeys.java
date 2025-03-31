package com.xianwei.customrpc.fault.retry;

/**
 * Retry Strategy Key Constants
 *
 * These keys are used to identify specific retry strategy implementations.
 */
public interface RetryStrategyKeys {

    /**
     * No retry
     */
    String NO = "no";

    /**
     * Fixed interval retry
     */
    String FIXED_INTERVAL = "fixedInterval";

}

