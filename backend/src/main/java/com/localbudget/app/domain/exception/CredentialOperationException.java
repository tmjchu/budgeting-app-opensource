package com.localbudget.app.domain.exception;

public class CredentialOperationException extends RuntimeException {

    public CredentialOperationException(String message) {
        super(message);
    }

    public CredentialOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
