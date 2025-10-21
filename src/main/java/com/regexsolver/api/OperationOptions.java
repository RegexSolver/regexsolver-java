package com.regexsolver.api;

public class OperationOptions {
    protected ResponseFormat responseFormat;
    protected Integer executionTimeout;

    public static OperationOptions newDefault() {
        return new OperationOptions();
    }

    public OperationOptions responseFormat(ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
        return this;
    }

    public ResponseFormat responseFormat() {
        return responseFormat;
    }

    public OperationOptions executionTimeout(Integer executionTimeout) {
        this.executionTimeout = executionTimeout;
        return this;
    }

    public Integer executionTimeout() {
        return executionTimeout;
    }
}
