package com.regexsolver.api.exceptions;

/** Raised when the provided JSON is invalid or cannot be parsed. */
public class InvalidJsonException extends BadRequestException {

    public InvalidJsonException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
