package com.regexsolver.api.exceptions;

/** Raised when the API returns a 403 Forbidden error. */
public class ForbiddenException extends ApiException {

    public ForbiddenException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
