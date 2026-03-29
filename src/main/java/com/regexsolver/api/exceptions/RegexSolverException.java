package com.regexsolver.api.exceptions;

/**
 * Base exception for all RegexSolver errors.
 */
public class RegexSolverException extends RuntimeException {

    public RegexSolverException(String message) {
        super(message);
    }
}
