package com.xianwei.customrpc.server;

import com.xianwei.customrpc.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * TCP Message Handler Wrapper
 *
 * This class wraps a raw TCP Buffer handler using the Decorator Pattern,
 * enhancing it with `RecordParser` to solve sticky and half-packet problems.
 * It ensures that a full message (header + body) is received before processing.
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    /**
     * RecordParser is used to extract complete messages from a TCP stream.
     * It prevents issues where messages are split or combined during transport.
     */
    private final RecordParser recordParser;

    /**
     * Constructor that wraps the given buffer handler with parsing logic.
     *
     * @param bufferHandler the original handler that should process complete messages
     */
    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        recordParser = initRecordParser(bufferHandler);
    }

    /**
     * Delegate incoming buffer data to the record parser.
     */
    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    /**
     * Initializes the RecordParser with logic to parse header and body.
     *
     * @param bufferHandler the handler to call once a full message is received
     * @return a configured RecordParser
     */
    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // Start by reading the fixed-length message header
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);

        parser.setOutput(new Handler<Buffer>() {
            int size = -1;                         // Size of the message body (read from header)
            Buffer resultBuffer = Buffer.buffer(); // Buffer to assemble full message

            @Override
            public void handle(Buffer buffer) {
                // Phase 1: Read the header
                if (size == -1) {
                    size = buffer.getInt(13);                // Body length is stored at byte offset 13
                    parser.fixedSizeMode(size);              // Switch parser to read the body next
                    resultBuffer.appendBuffer(buffer);       // Append header to result buffer
                } else {
                    // Phase 2: Read the body
                    resultBuffer.appendBuffer(buffer);       // Append body to result buffer
                    bufferHandler.handle(resultBuffer);      // Pass complete message to handler

                    // Reset parser to read the next message's header
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();          // Reset result buffer
                }
            }
        });

        return parser;
    }
}
