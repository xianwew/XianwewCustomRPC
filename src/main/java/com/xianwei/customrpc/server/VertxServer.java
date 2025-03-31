package com.xianwei.customrpc.server;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import lombok.extern.slf4j.Slf4j;

/**
 * Vert.x TCP Server
 *
 * This class starts a TCP server using Vert.x and registers a request handler
 * to process incoming RPC requests over raw TCP connections.
 */
@Slf4j // Lombok annotation for logging (generates a static `log` field)
public class VertxServer {

    /**
     * Starts the TCP server on the given port.
     *
     * @param port the port number to bind the server to
     */
    public void doStart(int port) {
        // Create a Vert.x instance (event loop engine)
        Vertx vertx = Vertx.vertx();

        // Create a NetServer for handling TCP connections
        NetServer server = vertx.createNetServer();

        // Register the custom request handler for incoming connections
        server.connectHandler(new TcpServerHandler());

        // Start listening on the specified port
        server.listen(port, result -> {
            if (result.succeeded()) {
                log.info("TCP server started on port " + port);
            } else {
                log.info("Failed to start TCP server: " + result.cause());
            }
        });
    }

    /**
     * Main method to start the server.
     */
    public static void main(String[] args) {
        new VertxServer().doStart(8888); // Start the server on port 8888
    }
}
