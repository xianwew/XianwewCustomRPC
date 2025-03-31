package com.xianwei.customrpc.protocol;

import com.xianwei.customrpc.serializer.Serializer;
import com.xianwei.customrpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * Protocol Message Encoder
 *
 * This class is responsible for encoding a ProtocolMessage (with header and body)
 * into a binary format (Buffer) for transmission over the network.
 */
public class ProtocolMessageEncoder {

    /**
     * Encode a ProtocolMessage into a Buffer
     *
     * @param protocolMessage The message to encode (either request or response)
     * @return A Vert.x Buffer containing the serialized message
     * @throws IOException if serialization fails
     */
    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {
        // Return an empty buffer if message or header is null
        if (protocolMessage == null || protocolMessage.getHeader() == null) {
            return Buffer.buffer();
        }

        // Extract the header
        ProtocolMessage.Header header = protocolMessage.getHeader();

        // Initialize a new buffer to hold the serialized data
        Buffer buffer = Buffer.buffer();

        // Append header fields in a fixed order
        buffer.appendByte(header.getMagic());        // 1 byte
        buffer.appendByte(header.getVersion());      // 1 byte
        buffer.appendByte(header.getSerializer());   // 1 byte
        buffer.appendByte(header.getType());         // 1 byte
        buffer.appendByte(header.getStatus());       // 1 byte
        buffer.appendLong(header.getRequestId());    // 8 bytes

        // Get serializer by key from header
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("Unsupported serialization protocol");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());

        // Serialize the message body
        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());

        // Append body length (4 bytes) and actual serialized body
        buffer.appendInt(bodyBytes.length);          // 4 bytes
        buffer.appendBytes(bodyBytes);

        return buffer;
    }
}
