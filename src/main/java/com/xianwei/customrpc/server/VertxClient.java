package com.xianwei.customrpc.server;

import cn.hutool.core.util.IdUtil;
import com.xianwei.customrpc.RpcApplication;
import com.xianwei.customrpc.model.RpcRequest;
import com.xianwei.customrpc.model.RpcResponse;
import com.xianwei.customrpc.model.ServiceMetaInfo;
import com.xianwei.customrpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Vert.x TCP Request Client
 *
 * This class is responsible for sending RPC requests over TCP to a remote service using Vert.x.
 * It constructs the protocol message, establishes a TCP connection, and decodes the server's response.
 */
public class VertxClient {

    /**
     * Send an RPC request and receive a response synchronously.
     *
     * @param rpcRequest the request object containing service name, method, args, etc.
     * @param serviceMetaInfo metadata of the target service (host, port)
     * @return the RPC response object returned by the server
     * @throws InterruptedException if the thread is interrupted while waiting
     * @throws ExecutionException if the async execution fails
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo)
            throws InterruptedException, ExecutionException {

        // Create a Vert.x instance and a NetClient to handle TCP
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();

        // Connect to the server
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(), result -> {
            if (!result.succeeded()) {
                System.err.println("Failed to connect to TCP server");
                return;
            }

            NetSocket socket = result.result();

            // --- Build the protocol message ---
            ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
            ProtocolMessage.Header header = new ProtocolMessage.Header();
            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
            header.setSerializer((byte) ProtocolMessageSerializerEnum
                    .getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
            header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
            header.setRequestId(IdUtil.getSnowflakeNextId());

            protocolMessage.setHeader(header);
            protocolMessage.setBody(rpcRequest);

            // --- Encode and send the request ---
            try {
                Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                socket.write(encodeBuffer);
            } catch (IOException e) {
                throw new RuntimeException("Protocol message encoding error", e);
            }

            // --- Receive and decode the response ---
            TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
                try {
                    ProtocolMessage<RpcResponse> rpcResponseProtocolMessage =
                            (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                    responseFuture.complete(rpcResponseProtocolMessage.getBody());
                } catch (IOException e) {
                    throw new RuntimeException("Protocol message decoding error", e);
                }
            });

            socket.handler(bufferHandlerWrapper);
        });

        // Wait for the async response
        RpcResponse rpcResponse = responseFuture.get();

        // Close the TCP client after the request is done
        netClient.close();

        return rpcResponse;
    }
}
