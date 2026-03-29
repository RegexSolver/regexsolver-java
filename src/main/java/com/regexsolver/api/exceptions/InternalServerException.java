package com.regexsolver.api.exceptions;

/**
 * Raised when the API returns a 500 Internal Server Error.
 * Indicates an unexpected failure or panic on the RegexSolver compute servers.
 */
public class InternalServerException extends ApiException {

    public InternalServerException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
