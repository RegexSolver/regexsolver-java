package com.regexsolver.api.exceptions;

/** Raised when the API returns a 404 Not Found error.
 * Indicates that the requested API endpoint or resource does not exist.
 */
public class NotFoundException extends ApiException {

    public NotFoundException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
