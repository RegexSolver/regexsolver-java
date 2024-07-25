package com.regexsolver.api.exception;

/**
 * Thrown if the API token has not been set as environment variable.
 */
public class MissingAPITokenException extends RuntimeException {
    /**
     * The API token has not been set, call RegexSolverApiWrapper.initialize(\"YOUR_TOKEN\"); to set it.
     * To generate a token go to <a href="https://console.regexsolver.com/">RegexSolver Console</a>.
     */
    public MissingAPITokenException() {
        super("The API token has not been set, call RegexSolverApiWrapper.initialize(\"YOUR_TOKEN\") to set it.\n" +
                "To generate a token go to https://console.regexsolver.com/.");
    }
}
