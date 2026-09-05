package com.localbudget.app.domain.exception;

public class PlaidCredentialsUnavailableException extends RuntimeException {

    public PlaidCredentialsUnavailableException() {
        super("Plaid credentials are not available. Complete setup or unlock the app.");
    }
}
