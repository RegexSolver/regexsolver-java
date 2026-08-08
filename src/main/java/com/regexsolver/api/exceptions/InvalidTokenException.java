package com.regexsolver.api.exceptions;

/** Raised when the provided authentication token is invalid. */
public class InvalidTokenException extends UnauthorizedException {

    public InvalidTokenException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
