package com.regexsolver.api.exceptions;

/**
 * Raised when the API returns a 429 Too Many Requests error and max retries are exceeded.
 * Indicates that your requests-per-second (req/s) rate limit has been exceeded.
 */
public class TooManyRequestsException extends ApiException {

    public TooManyRequestsException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
