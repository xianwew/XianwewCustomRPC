package com.xianwei.customrpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC Response
 *
 * This class represents the response returned from a Remote Procedure Call (RPC).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {

    /**
     * The response data returned from the invoked method
     */
    private Object data;

    /**
     * Type of the response data (reserved for future use, e.g., deserialization hints)
     */
    private Class<?> dataType;

    /**
     * Additional message, such as "success" or custom info
     */
    private String message;

    /**
     * Exception details if the RPC call failed or threw an error
     */
    private Exception exception;

}
