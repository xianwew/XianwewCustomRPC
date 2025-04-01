package com.xianwei.customrpc.protocol;

/**
 * Protocol Constant
 *
 */
public interface ProtocolConstant {

    /**
     * Head length
     */
    int MESSAGE_HEADER_LENGTH = 17;

    /**
     * Magic byte for safety
     */
    byte PROTOCOL_MAGIC = 0x1;

    /**
     * Version number
     */
    byte PROTOCOL_VERSION = 0x1;
}
