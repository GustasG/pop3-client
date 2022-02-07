package org.guge.pop3.errors;

public class InvalidSpecificationException extends ResponseException {
    public InvalidSpecificationException() {
    }

    public InvalidSpecificationException(String message) {
        super(message);
    }

    public InvalidSpecificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSpecificationException(Throwable cause) {
        super(cause);
    }
}