package com.xianwei.customrpc.protocol;

import lombok.Getter;

/**
 * Protocol Message Type Enum
 *
 * This enum defines the types of messages supported by the RPC protocol.
 * These are encoded in the message header to determine how the message should be handled.
 */
@Getter // Lombok: generates getter for the 'key' field
public enum ProtocolMessageTypeEnum {

    REQUEST(0),      // Represents a client-to-server RPC request
    RESPONSE(1),     // Represents a server-to-client RPC response
    HEART_BEAT(2),   // Used for heartbeat/ping messages to keep the connection alive
    OTHERS(3);       // Placeholder for other message types (future extension)

    /**
     * Integer identifier for the message type (used in protocol header)
     */
    private final int key;

    ProtocolMessageTypeEnum(int key) {
        this.key = key;
    }

    /**
     * Get the enum constant by its numeric key
     *
     * @param key the integer key from the protocol header
     * @return the corresponding enum, or null if no match is found
     */
    public static ProtocolMessageTypeEnum getEnumByKey(int key) {
        for (ProtocolMessageTypeEnum anEnum : ProtocolMessageTypeEnum.values()) {
            if (anEnum.key == key) {
                return anEnum;
            }
        }
        return null;
    }
}
