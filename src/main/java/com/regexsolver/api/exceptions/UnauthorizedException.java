package com.regexsolver.api.exceptions;

/** Raised when the API returns a 401 Unauthorized error. */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
