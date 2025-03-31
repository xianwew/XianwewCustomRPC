package com.xianwei.customrpc.protocol;

import lombok.Getter;

/**
 * Protocol Message Status Enum
 *
 * This enum defines the possible status codes for protocol messages.
 * These can be used to indicate the result of a request or response.
 */
@Getter // Lombok: generates getters for all fields
public enum ProtocolMessageStatusEnum {

    OK("ok", 20),                        // Successful request/response
    BAD_REQUEST("badRequest", 40),      // Invalid or malformed request
    BAD_RESPONSE("badResponse", 50);    // Error occurred while processing response

    /**
     * Human-readable description of the status
     */
    private final String text;

    /**
     * Numeric value used in the protocol header (status byte)
     */
    private final int value;

    ProtocolMessageStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * Get enum instance by numeric value
     *
     * @param value the status code from the message header
     * @return the matching enum constant, or null if not found
     */
    public static ProtocolMessageStatusEnum getEnumByValue(int value) {
        for (ProtocolMessageStatusEnum anEnum : ProtocolMessageStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
}
