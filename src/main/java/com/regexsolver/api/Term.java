package com.regexsolver.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.regexsolver.api.Request.MultiTermsRequest;
import com.regexsolver.api.dto.Details;
import com.regexsolver.api.exception.ApiError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This abstract class represents a term on which it is possible to perform operations.
 */
public abstract class Term implements ResponseContent {
    @JsonIgnore
    private final static String REGEX_PREFIX = "regex";
    @JsonIgnore
    private final static String FAIR_PREFIX = "fair";
    @JsonIgnore
    private final static String UNKNOWN_PREFIX = "unknown";

    private final String value;

    @JsonIgnore
    private transient String serialized = null;

    @JsonIgnore
    private transient Details details;

    /**
     * Create a new instance.
     *
     * @param value The value of the term.
     */
    protected Term(String value) {
        this.value = value;
    }

    /**
     * Create a new instance of {@link Term.Regex}.
     *
     * @param regex The regular expression pattern.
     * @return The created instance.
     */
    public static Term.Regex regex(String regex) {
        return new Term.Regex(regex);
    }

    /**
     * Create a new instance of {@link Term.Fair}.
     *
     * @param fair The FAIR.
     * @return The created instance.
     */
    public static Term.Fair fair(String fair) {
        return new Term.Fair(fair);
    }

    String getValue() {
        return value;
    }

    /**
     * Get the details of this term.
     * Cache the result to avoid calling the API again if this method is called multiple times.
     *
     * @return The details of this term.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Details getDetails() throws IOException, ApiError {
        if (details != null) {
            return details;
        }
        details = RegexSolverApiWrapper.getInstance().getDetails(this);
        return details;
    }

    /**
     * Generate the given number of unique strings matched by this term.
     *
     * @param count The number of unique strings to generate.
     * @return A list of unique strings matched by this term.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public List<String> generateStrings(int count) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance().generateStrings(this, count);
    }

    @JsonIgnore
    private List<Term> getArgs(Term... terms) {
        ArrayList<Term> args = new ArrayList<>();
        args.add(this);
        args.addAll(List.of(terms));
        return args;
    }

    /**
     * Compute the intersection with the given terms and return the resulting term.
     *
     * @param terms The terms to compute an intersection with.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term intersection(Term... terms) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .computeIntersection(new MultiTermsRequest(getArgs(terms)));
    }

    /**
     * Compute the union with the given terms and return the resulting term.
     *
     * @param terms The terms to compute a union with.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term union(Term... terms) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .computeUnion(new MultiTermsRequest(getArgs(terms)));
    }

    /**
     * Compute the subtraction with the given term and return the resulting term.
     *
     * @param term The term to subtract.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term subtraction(Term term) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .computeSubtraction(new MultiTermsRequest(getArgs(term)));
    }

    /**
     * Check equivalence with the given term.
     *
     * @param term The term to check equivalence with.
     * @return true if the terms are equivalent, false otherwise.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public boolean isEquivalentTo(Term term) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .equivalence(new MultiTermsRequest(getArgs(term)));
    }

    /**
     * Check if is a subset of the given term.
     *
     * @param term The term to check if is the superset of this.
     * @return true if this is a subset, false otherwise.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public boolean isSubsetOf(Term term) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .subset(new MultiTermsRequest(getArgs(term)));
    }

    /**
     * Generate a string representation that can be parsed by {@link #deserialize(String)}.
     *
     * @return A string representation of this term.
     */
    public String serialize() {
        if (serialized != null) {
            return serialized;
        }
        String prefix;
        if (this instanceof Regex) {
            prefix = REGEX_PREFIX;
        } else if (this instanceof Fair) {
            prefix = FAIR_PREFIX;
        } else {
            prefix = UNKNOWN_PREFIX;
        }
        serialized = String.format("%s=%s", prefix, value);
        return serialized;
    }

    /**
     * Parse a string representation of a {@link Term} produced by {@link #serialize()}.
     *
     * @param string A string representation produced by {@link #serialize()}.
     * @return The parsed term, or empty if the method was not able to parse.
     */
    @JsonIgnore
    public static Optional<Term> deserialize(String string) {
        if (string == null) {
            return Optional.empty();
        }

        if (string.startsWith(REGEX_PREFIX)) {
            return Optional.of(regex(string.substring(REGEX_PREFIX.length() + 1)));
        } else if (string.startsWith(FAIR_PREFIX)) {
            return Optional.of(fair(string.substring(FAIR_PREFIX.length() + 1)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term = (Term) o;
        return Objects.equals(term.serialize(), serialize());
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialize());
    }

    @Override
    public String toString() {
        return serialize();
    }

    /**
     * This term represents a Fast Automaton Internal Representation (FAIR).
     * <p>
     * You can learn more about FAIR in our <a href="https://docs.regexsolver.com/" target="_blank">documentation</a>.
     * </p>
     */
    public static final class Fair extends Term {
        /**
         * Create a new instance.
         *
         * @param fair The FAIR.
         */
        public Fair(@JsonProperty("value") String fair) {
            super(fair);
        }

        /**
         * Return the Fast Automaton Internal Representation (FAIR).
         *
         * @return The FAIR.
         */
        @JsonProperty("value")
        public String getFair() {
            return getValue();
        }
    }


    /**
     * This term represents a regular expression.
     * <p>
     * You can learn more about regular expression in our <a href="https://docs.regexsolver.com/" target="_blank">documentation</a>
     * </p>
     */
    public static final class Regex extends Term {
        /**
         * Create a new instance.
         *
         * @param regex The regular expression pattern.
         */
        public Regex(@JsonProperty("value") String regex) {
            super(regex);
        }

        /**
         * Return the regular expression pattern.
         *
         * @return The regular expression pattern.
         */
        @JsonProperty("value")
        public String getPattern() {
            return getValue();
        }
    }
}
