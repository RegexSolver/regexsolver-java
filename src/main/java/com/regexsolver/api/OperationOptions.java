package com.regexsolver.api;

import java.util.Optional;

/**
 * Options for RegexSolver operations.
 *
 * <p>Not every option is relevant to every operation: an option is ignored by any operation
 * it does not apply to. For instance the options describing the returned term have no effect
 * on an operation that does not return one.
 */
public class OperationOptions {

    private Integer executionTimeout;
    private ResponseFormat responseFormat;
    private Boolean deterministic;

    public OperationOptions() {}

    public OperationOptions(
        Integer executionTimeout,
        ResponseFormat responseFormat
    ) {
        this.executionTimeout = executionTimeout;
        this.responseFormat = responseFormat;
    }

    public static OperationOptions builder() {
        return new OperationOptions();
    }

    /**
     * Maximum time, in milliseconds, the engine may spend on the operation before aborting it.
     */
    public OperationOptions executionTimeout(Integer timeout) {
        this.executionTimeout = timeout;
        return this;
    }

    /**
     * Format of the term returned by the operation.
     */
    public OperationOptions responseFormat(ResponseFormat format) {
        this.responseFormat = format;
        return this;
    }

    /**
     * When true, guarantees the returned FAIR encodes a deterministic automaton.
     * Only valid with responseFormat = ResponseFormat.FAIR or when responseFormat is
     * unset (in which case it defaults to ResponseFormat.FAIR). Throws otherwise.
     */
    public OperationOptions deterministic(Boolean deterministic) {
        this.deterministic = deterministic;
        return this;
    }

    public Optional<Integer> getExecutionTimeout() {
        return Optional.ofNullable(executionTimeout);
    }

    public Optional<ResponseFormat> getResponseFormat() {
        return Optional.ofNullable(responseFormat);
    }

    public Optional<Boolean> getDeterministic() {
        return Optional.ofNullable(deterministic);
    }
}
