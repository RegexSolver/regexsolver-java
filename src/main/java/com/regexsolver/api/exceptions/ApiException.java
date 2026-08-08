package com.regexsolver.api.exceptions;

/** Base exception raised when the RegexSolver API returns an error response. */
public class ApiException extends RegexSolverException {

    private final int statusCode;
    private final String errorCode;
    private final String body;

    public ApiException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getBody() {
        return body;
    }
}
