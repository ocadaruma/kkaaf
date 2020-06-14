package com.mayreh.toyka.network;

import java.io.Serializable;
import java.net.Socket;
import java.util.Objects;

/**
 * Unique connection id assigned to each socket connection
 */
public class ConnectionId implements Serializable {
    private static final long serialVersionUID = -2008785333620833489L;

    private final String remoteHost;
    private final int remotePort;
    private final String localHost;
    private final int localPort;
    private final int sequence;

    public ConnectionId(Socket socket, int sequence) {
        this(socket.getInetAddress().getHostAddress(),
                socket.getPort(),
                socket.getLocalAddress().getHostAddress(),
                socket.getLocalPort(),
                sequence);
    }

    public ConnectionId(String remoteHost,
                        int remotePort,
                        String localHost,
                        int localPort,
                        int sequence) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.localHost = localHost;
        this.localPort = localPort;

        this.sequence = sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnectionId other = (ConnectionId) o;
        return remotePort == other.remotePort &&
               localPort == other.localPort &&
               sequence == other.sequence &&
               remoteHost.equals(other.remoteHost) &&
               localHost.equals(other.localHost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                remoteHost,
                remotePort,
                localHost,
                localPort,
                sequence);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(remoteHost).append(':')
                .append(remotePort)
                .append('-')
                .append(localHost).append(':')
                .append(localPort)
                .append('-')
                .append(sequence).toString();
    }
}
