package com.regexsolver.api.exceptions;

/** Raised when your account's monthly compute quota has been exceeded. */
public class QuotaExceededException extends ForbiddenException {

    public QuotaExceededException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
