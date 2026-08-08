package com.regexsolver.api.exceptions;

/**
 * Raised when the provided FAIR value is malformed or cannot be decoded.
 */
public class FairSyntaxException extends BadRequestException {
    public FairSyntaxException(String message, int statusCode, String errorCode, String body) {
        super(message, statusCode, errorCode, body);
    }
}
