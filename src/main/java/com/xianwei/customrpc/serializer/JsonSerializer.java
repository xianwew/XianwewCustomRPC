package com.xianwei.customrpc.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xianwei.customrpc.model.RpcRequest;
import com.xianwei.customrpc.model.RpcResponse;

import java.io.IOException;

/**
 * JSON Serializer
 *
 * Implements the Serializer interface using Jackson's ObjectMapper.
 * Handles standard JSON (de)serialization and fixes issues related to type erasure
 * when deserializing generic objects like method arguments and response data.
 */
public class JsonSerializer implements Serializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Serialize an object to a JSON byte array.
     */
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    /**
     * Deserialize a byte array into an object of the specified type.
     * Adds custom handling for RpcRequest and RpcResponse to ensure proper type restoration.
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> classType) throws IOException {
        T obj = OBJECT_MAPPER.readValue(bytes, classType);

        // Special handling for RpcRequest: fix deserialized argument types
        if (obj instanceof RpcRequest) {
            return handleRequest((RpcRequest) obj, classType);
        }

        // Special handling for RpcResponse: fix deserialized response data type
        if (obj instanceof RpcResponse) {
            return handleResponse((RpcResponse) obj, classType);
        }

        return obj;
    }

    /**
     * Fix deserialization of RpcRequest due to type erasure of Object[] args.
     * Each argument may be deserialized as LinkedHashMap instead of its original class.
     *
     * @param rpcRequest the request object with raw deserialized args
     * @param type the target class (RpcRequest)
     */
    private <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> expectedClass = parameterTypes[i];
            Object actualArg = args[i];

            // If types do not match, re-serialize and re-deserialize to correct type
            if (!expectedClass.isAssignableFrom(actualArg.getClass())) {
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(actualArg);
                args[i] = OBJECT_MAPPER.readValue(argBytes, expectedClass);
            }
        }

        return type.cast(rpcRequest);
    }

    /**
     * Fix deserialization of RpcResponse due to type erasure of the generic data field.
     *
     * @param rpcResponse the response object with raw data
     * @param type the target class (RpcResponse)
     */
    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        // Deserialize the 'data' field into its original declared type
        byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
        Object correctData = OBJECT_MAPPER.readValue(dataBytes, rpcResponse.getDataType());
        rpcResponse.setData(correctData);
        return type.cast(rpcResponse);
    }
}

