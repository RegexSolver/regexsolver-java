package com.regexsolver.api;

import java.util.Optional;

/**
 * Options for generateStrings(), extending {@link OperationOptions} so it is
 * accepted wherever an options object is.
 */
public final class GenerateStringsOptions extends OperationOptions {

    private PathOrder pathOrder;
    private CharacterOrder characterOrder;
    private Long seed;
    private Integer minLength;
    private Integer maxLength;
    private String charset;

    public GenerateStringsOptions() {}

    public static GenerateStringsOptions builder() {
        return new GenerateStringsOptions();
    }

    @Override
    public GenerateStringsOptions executionTimeout(Integer timeout) {
        super.executionTimeout(timeout);
        return this;
    }

    /**
     * Order in which the paths (shapes) of the language are scheduled.
     * Defaults to {@link PathOrder#SWEEP}.
     */
    public GenerateStringsOptions pathOrder(PathOrder pathOrder) {
        this.pathOrder = pathOrder;
        return this;
    }

    /**
     * Order in which the strings within each path are produced. Defaults to
     * {@link CharacterOrder#ASCENDING}.
     */
    public GenerateStringsOptions characterOrder(CharacterOrder characterOrder) {
        this.characterOrder = characterOrder;
        return this;
    }

    /**
     * Seed behind the shuffled modes. The default seed is fixed, so two calls
     * sharing a seed generate the same strings and {@code offset} pages
     * through them consistently.
     */
    public GenerateStringsOptions seed(Long seed) {
        this.seed = seed;
        return this;
    }

    /**
     * Shortest string to generate. Shorter strings are left out of the
     * enumeration entirely, {@code offset} never counting them.
     */
    public GenerateStringsOptions minLength(Integer minLength) {
        this.minLength = minLength;
        return this;
    }

    /**
     * Longest string to generate.
     */
    public GenerateStringsOptions maxLength(Integer maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * Restricts generation to the given characters, e.g. {@code [a-z]}. Paths
     * requiring a character outside it are dropped.
     */
    public GenerateStringsOptions charset(String charset) {
        this.charset = charset;
        return this;
    }

    public Optional<PathOrder> getPathOrder() {
        return Optional.ofNullable(pathOrder);
    }

    public Optional<CharacterOrder> getCharacterOrder() {
        return Optional.ofNullable(characterOrder);
    }

    public Optional<Long> getSeed() {
        return Optional.ofNullable(seed);
    }

    public Optional<Integer> getMinLength() {
        return Optional.ofNullable(minLength);
    }

    public Optional<Integer> getMaxLength() {
        return Optional.ofNullable(maxLength);
    }

    public Optional<String> getCharset() {
        return Optional.ofNullable(charset);
    }
}
