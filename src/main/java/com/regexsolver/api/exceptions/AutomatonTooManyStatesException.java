package com.regexsolver.api.exceptions;

/**
 * Raised when the NFA/DFA exceeds the maximum allowed number of states for your current plan.
 */
public class AutomatonTooManyStatesException extends BadRequestException {
    public AutomatonTooManyStatesException(String message, int statusCode, String errorCode, String body) {
        super(message, statusCode, errorCode, body);
    }
}
