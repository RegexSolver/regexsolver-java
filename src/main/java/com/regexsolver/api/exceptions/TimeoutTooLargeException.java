package com.regexsolver.api.exceptions;

/** Raised when the requested `execution_timeout` exceeds the maximum allowed for your current plan. */
public class TimeoutTooLargeException extends BadRequestException {

    public TimeoutTooLargeException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
