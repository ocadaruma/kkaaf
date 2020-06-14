package com.mayreh.toyka.network;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Nio socket connection.
 *
 * This class just holds send/receive status of the connection
 */
public class Connection {

    public static class ReceiveResult {
        private final RequestResponseReceive receive;

        public ReceiveResult(RequestResponseReceive receive) {
            this.receive = receive;
        }

        public boolean completed() {
            return receive != null;
        }

        public RequestResponseReceive receive() {
            return receive;
        }
    }

    public static class SendResult {
        private final boolean completed;

        public boolean completed() {
            return completed;
        }
    }

    static class ReceiveStateMachine {

    }

    static class SendStateMachine {

    }

    /**
     * Receive data from channel and maybe construct {@link RequestResponseReceive} instance
     */
    public ReceiveResult receiveFrom(ReadableByteChannel channel) {

    }

    public void setSend(RequestResponseSend send) {

    }

    public SendResult transferTo(WritableByteChannel channel) {
    }
}
