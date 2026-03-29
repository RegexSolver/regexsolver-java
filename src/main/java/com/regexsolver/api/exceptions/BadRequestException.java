package com.regexsolver.api.exceptions;

/** Raised when the API returns a 400 Bad Request error. */
public class BadRequestException extends ApiException {

    public BadRequestException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
