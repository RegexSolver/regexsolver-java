package com.regexsolver.api.exceptions;

/** Raised when the number of terms provided exceeds the maximum allowed. */
public class TooManyTermsException extends BadRequestException {

    public TooManyTermsException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
