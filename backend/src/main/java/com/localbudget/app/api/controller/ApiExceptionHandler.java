package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.ErrorResponse;
import com.localbudget.app.domain.exception.CredentialOperationException;
import com.localbudget.app.domain.exception.PlaidCredentialsUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CredentialOperationException.class)
    ResponseEntity<ErrorResponse> handleCredentialOperation(
            CredentialOperationException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(PlaidCredentialsUnavailableException.class)
    ResponseEntity<ErrorResponse> handleCredentialsUnavailable(
            PlaidCredentialsUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation() {
        return ResponseEntity.badRequest().body(new ErrorResponse("Request validation failed."));
    }
}
