package com.regexsolver.api.exceptions;

/** Raised when fewer terms are provided than the operation requires. */
public class TooFewTermsException extends BadRequestException {

    public TooFewTermsException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
