package org.guge.pop3;

public enum State {
    AUTHORIZATION, // TCP connection has been established, but user has not authenticated themselves
    TRANSACTION, // Server has authenticated given client
    Update // TODO
}
