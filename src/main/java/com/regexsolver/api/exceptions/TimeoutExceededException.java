package com.regexsolver.api.exceptions;

/** Raised when the execution of the request exceeds the provided `execution_timeout` or the maximum allowed for your current plan. */
public class TimeoutExceededException extends BadRequestException {

    public TimeoutExceededException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
