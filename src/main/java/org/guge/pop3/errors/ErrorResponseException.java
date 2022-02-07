package org.guge.pop3.errors;

public class ErrorResponseException extends ResponseException {
    public ErrorResponseException() {
    }

    public ErrorResponseException(String message) {
        super(message);
    }

    public ErrorResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorResponseException(Throwable cause) {
        super(cause);
    }
}