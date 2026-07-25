package com.regexsolver.api;

import java.util.Optional;

/**
 * Options accepted by every RegexSolver operation.
 *
 * <p>Analyze operations and {@code determinize()} take this type rather than
 * {@link OperationOptions}: they do not return a caller-shaped term, so
 * {@code responseFormat} and {@code deterministic} would have no effect there.
 * The two types are deliberately unrelated so that passing the wrong one is a
 * compile error instead of a silently ignored field.</p>
 */
public class ExecutionOptions {

    private Integer executionTimeout;

    public ExecutionOptions() {}

    public ExecutionOptions(Integer executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    public static ExecutionOptions builder() {
        return new ExecutionOptions();
    }

    public ExecutionOptions executionTimeout(Integer timeout) {
        this.executionTimeout = timeout;
        return this;
    }

    public Optional<Integer> getExecutionTimeout() {
        return Optional.ofNullable(executionTimeout);
    }
}
