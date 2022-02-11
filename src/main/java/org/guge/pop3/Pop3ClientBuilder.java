package org.guge.pop3;

import java.io.IOException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class Pop3ClientBuilder {
    private String host;
    private int port;
    private String username;
    private String password;
    private boolean useSsl;
    private int timeout;

    public Pop3ClientBuilder() {
        timeout = 15 * 1_000;
    }

    public Pop3ClientBuilder useHost(String host) {
        this.host = host;
        return this;
    }

    public Pop3ClientBuilder usePort(int port) {
        this.port = port;
        return this;
    }

    public Pop3ClientBuilder useCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public Pop3ClientBuilder useSSL(boolean useSsl) {
        this.useSsl = useSsl;
        return this;
    }

    public Pop3ClientBuilder useTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public Pop3Client build() throws IOException {
        var socket = getSocketFactory()
                .createSocket(host, port);

        var client = new Pop3Client(socket);
        client.setTimeout(timeout);

        if (username != null && password != null) {
            client.authenticate(username, password);
        }

        return client;
    }

    private SocketFactory getSocketFactory() {
        if (useSsl) {
            return SSLSocketFactory.getDefault();
        } else {
            return SocketFactory.getDefault();
        }
    }
}
