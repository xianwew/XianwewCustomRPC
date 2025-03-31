package com.xianwei.customrpc.protocol;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Protocol Message Serializer Enum
 *
 * This enum defines the supported serialization methods for protocol messages.
 */
@Getter // Lombok: automatically generates getters for all fields
public enum ProtocolMessageSerializerEnum {

    JDK(0, "jdk"),         // Java built-in serialization
    JSON(1, "json"),       // JSON serialization
    KRYO(2, "kryo"),       // Kryo serialization
    HESSIAN(3, "hessian"); // Hessian serialization

    /**
     * Numeric identifier for the serializer (used in protocol headers)
     */
    private final int key;

    /**
     * Human-readable name for the serializer
     */
    private final String value;

    ProtocolMessageSerializerEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get a list of all serializer names (e.g., ["jdk", "json", "kryo", "hessian"])
     *
     * @return list of string values
     */
    public static List<String> getValues() {
        return Arrays.stream(values())
                .map(item -> item.value)
                .collect(Collectors.toList());
    }

    /**
     * Get enum instance by numeric key
     *
     * @param key serializer ID from protocol header
     * @return matching enum or null if not found
     */
    public static ProtocolMessageSerializerEnum getEnumByKey(int key) {
        for (ProtocolMessageSerializerEnum anEnum : ProtocolMessageSerializerEnum.values()) {
            if (anEnum.key == key) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * Get enum instance by string value (name)
     *
     * @param value serializer name
     * @return matching enum or null if not found or value is empty
     */
    public static ProtocolMessageSerializerEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (ProtocolMessageSerializerEnum anEnum : ProtocolMessageSerializerEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
