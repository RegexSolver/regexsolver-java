package com.regexsolver.api.exceptions;

/**
 * Raised when the provided regular expression has invalid syntax.
 */
public class RegexSyntaxException extends BadRequestException {
    public RegexSyntaxException(String message, int statusCode, String errorCode, String body) {
        super(message, statusCode, errorCode, body);
    }
}
