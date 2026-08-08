package com.regexsolver.api.exceptions;

/** Raised when the provided authentication token is missing or malformed. */
public class MissingOrMalformedTokenException extends UnauthorizedException {

    public MissingOrMalformedTokenException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
