package org.guge.pop3;

import java.io.IOException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class Pop3ClientFactory {
    private String host;
    private int port;
    private String username;
    private String password;
    private boolean useSsl;

    public Pop3ClientFactory useHost(String host) {
        this.host = host;
        return this;
    }

    public Pop3ClientFactory usePort(int port) {
        this.port = port;
        return this;
    }

    public Pop3ClientFactory useCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public Pop3ClientFactory useSSL(boolean useSsl) {
        this.useSsl = useSsl;
        return this;
    }

    public Pop3Client build() throws IOException {
        var socket = getSocketFactory()
                .createSocket(host, port);

        if (username != null && password != null) {
            return new Pop3Client(socket, username, password);
        }

        return new Pop3Client(socket);
    }

    private SocketFactory getSocketFactory() {
        if (useSsl) {
            return SSLSocketFactory.getDefault();
        } else {
            return SocketFactory.getDefault();
        }
    }
}
