package com.regexsolver.api.exceptions;

/** Raised when the requested number of strings to generate is below the minimum or exceeds the maximum allowed. */
public class InvalidNumberOfStringsToGenerateException
    extends BadRequestException
{

    public InvalidNumberOfStringsToGenerateException(
        String message,
        int statusCode,
        String errorCode,
        String body
    ) {
        super(message, statusCode, errorCode, body);
    }
}
