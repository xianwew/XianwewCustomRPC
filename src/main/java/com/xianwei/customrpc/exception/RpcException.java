package com.xianwei.customrpc.exception;

/**
 * Custom Exception Class for the RPC Framework
 */
public class RpcException extends RuntimeException {

    /**
     * Constructs a new RpcException with the specified error message.
     *
     * @param message The detail message for the exception
     */
    public RpcException(String message) {
        super(message);
    }

}

