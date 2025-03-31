package com.xianwei.customrpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Protocol Message Structure
 *
 * This class defines the structure of a protocol message used for RPC communication.
 * It includes a fixed-format header and a body (which can be a request or response).
 *
 * @param <T> The type of the message body (e.g., RpcRequest or RpcResponse)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {

    /**
     * The message header containing metadata and control information.
     */
    private Header header;

    /**
     * The message body, which could be an RpcRequest or RpcResponse object.
     */
    private T body;

    /**
     * Inner static class defining the structure of the protocol header.
     * Used to ensure message framing, versioning, and control.
     */
    @Data
    public static class Header {

        /**
         * Magic number to validate that the message conforms to the expected protocol.
         * Helps detect invalid or malicious messages early.
         */
        private byte magic;

        /**
         * Protocol version number.
         * Allows compatibility management between different client/server versions.
         */
        private byte version;

        /**
         * Serializer identifier.
         * Specifies which serialization method is used (e.g., JSON, Kryo, Protobuf).
         */
        private byte serializer;

        /**
         * Message type identifier.
         * Indicates if the message is a request or response (e.g., 0 = request, 1 = response).
         */
        private byte type;

        /**
         * Status code.
         * Used primarily in responses to indicate success or error states.
         */
        private byte status;

        /**
         * Unique request ID to correlate requests with responses.
         * Helps support asynchronous and concurrent communication.
         */
        private long requestId;

        /**
         * Length of the message body in bytes.
         * Used to properly read and decode the message payload.
         */
        private int bodyLength;
    }

}
