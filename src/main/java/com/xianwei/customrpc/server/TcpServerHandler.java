package com.xianwei.customrpc.server;

import com.xianwei.customrpc.model.RpcRequest;
import com.xianwei.customrpc.model.RpcResponse;
import com.xianwei.customrpc.protocol.*;
import com.xianwei.customrpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * TCP Request Handler
 *
 * This class handles incoming TCP connections and processes RPC requests.
 * It uses the TcpBufferHandlerWrapper to ensure complete messages are received,
 * then decodes, invokes, and responds to RPC requests.
 */
public class TcpServerHandler implements Handler<NetSocket> {

    /**
     * Handles new client socket connections.
     *
     * @param socket the connected TCP socket
     */
    @Override
    public void handle(NetSocket socket) {
        // Wrap the socket's data handler with a parser to handle full messages
        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            ProtocolMessage<RpcRequest> protocolMessage;

            // Decode the protocol message from the raw TCP buffer
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("Protocol message decoding error", e);
            }

            RpcRequest rpcRequest = protocolMessage.getBody();
            ProtocolMessage.Header header = protocolMessage.getHeader();

            // Build the response
            RpcResponse rpcResponse = new RpcResponse();

            try {
                // Look up the service implementation class
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());

                // Use reflection to find and invoke the target method
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());

                // Populate response with result
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");

            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // Encode and send the response back to the client
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());

            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);

            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                socket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("Protocol message encoding error", e);
            }
        });

        // Register the wrapped buffer handler to process incoming socket data
        socket.handler(bufferHandlerWrapper);
    }
}
