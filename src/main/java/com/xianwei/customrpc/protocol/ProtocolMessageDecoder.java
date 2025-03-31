package com.xianwei.customrpc.protocol;

import com.xianwei.customrpc.model.RpcRequest;
import com.xianwei.customrpc.model.RpcResponse;
import com.xianwei.customrpc.serializer.Serializer;
import com.xianwei.customrpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * Protocol Message Decoder
 *
 * This class is responsible for decoding raw byte data from the buffer
 * into a structured ProtocolMessage (either RpcRequest or RpcResponse).
 */
public class ProtocolMessageDecoder {

    /**
     * Decode a ProtocolMessage from the buffer
     *
     * @param buffer The raw byte buffer containing the message
     * @return A decoded ProtocolMessage instance
     * @throws IOException if deserialization fails
     */
    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        // Create an empty header to fill in
        ProtocolMessage.Header header = new ProtocolMessage.Header();

        // Read and validate the magic number at position 0
        byte magic = buffer.getByte(0);
        if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new RuntimeException("Invalid magic number in message");
        }
        header.setMagic(magic);

        // Read remaining header fields from fixed offsets
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));     // request ID spans bytes 5–12
        header.setBodyLength(buffer.getInt(13));    // body length spans bytes 13–16

        // Solve sticky packet issues by reading only the specified body length
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());

        // Get serializer based on the serializer key in the header
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("Unsupported serialization protocol");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());

        // Get the message type (e.g., REQUEST, RESPONSE)
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if (messageTypeEnum == null) {
            throw new RuntimeException("Unsupported message type");
        }

        // Deserialize based on message type
        switch (messageTypeEnum) {
            case REQUEST:
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                return new ProtocolMessage<>(header, request);
            case RESPONSE:
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                return new ProtocolMessage<>(header, response);
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new RuntimeException("Message type not supported yet");
        }
    }
}
