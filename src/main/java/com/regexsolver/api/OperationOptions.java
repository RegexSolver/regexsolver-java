package com.regexsolver.api;

import java.util.Optional;

/**
 * Options for RegexSolver operations.
 */
public class OperationOptions {

    private Integer executionTimeout;
    private ResponseFormat responseFormat;

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

    public OperationOptions executionTimeout(Integer timeout) {
        this.executionTimeout = timeout;
        return this;
    }

    public OperationOptions responseFormat(ResponseFormat format) {
        this.responseFormat = format;
        return this;
    }

    public Optional<Integer> getExecutionTimeout() {
        return Optional.ofNullable(executionTimeout);
    }

    public Optional<ResponseFormat> getResponseFormat() {
        return Optional.ofNullable(responseFormat);
    }
}
