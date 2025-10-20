package com.regexsolver.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.regexsolver.api.Request.GenerateStringsRequest;
import com.regexsolver.api.Request.MultiTermsRequest;
import com.regexsolver.api.Request.RepeatRequest;
import com.regexsolver.api.Request.RequestOptions;
import com.regexsolver.api.Request.RequestOptions.ResponseFormat;
import com.regexsolver.api.dto.Cardinality;
import com.regexsolver.api.dto.Details;
import com.regexsolver.api.dto.Length;
import com.regexsolver.api.exception.ApiError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This abstract class represents a term on which it is possible to perform
 * operations.
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
    @JsonIgnore
    private transient Cardinality cardinality;
    @JsonIgnore
    private transient Length length;
    @JsonIgnore
    private transient Boolean empty;
    @JsonIgnore
    private transient Boolean total;
    @JsonIgnore
    private transient Boolean emptyString;
    @JsonIgnore
    private transient String pattern;
    @JsonIgnore
    private transient String dot;

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

    public String getValue() {
        return value;
    }

    private static RequestOptions loadRequestOptions(OperationOptions opts) {
        RequestOptions requestOptions = null;
        if (opts != null) {
            requestOptions = RequestOptions.fromArgs(opts.responseFormat, opts.executionTimeout);
        }
        return requestOptions;
    }

    // Analyze

    /**
     * Check equivalence with the given term.
     *
     * @param opts Execution options.
     * @param term The term to check equivalence with.
     * @return true if the terms are equivalent, false otherwise.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public boolean equivalent(OperationOptions opts, Term term) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .analyzeEquivalent(new MultiTermsRequest(getArgs(term),
                        loadRequestOptions(opts)));
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
    public boolean equivalent(Term term) throws IOException, ApiError {
        return equivalent(null, term);
    }

    /**
     * Get the cardinality of this term.
     * Cache the result to avoid calling the API again if this method is called
     * multiple times.
     *
     * @return A `Cardinality` object describing how many distinct strings are
     *         matched.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Cardinality getCardinality() throws IOException, ApiError {
        if (cardinality != null) {
            return cardinality;
        } else if (details != null) {
            return details.getCardinality();
        }
        cardinality = RegexSolverApiWrapper.getInstance()
                .analyzeCardinality(this);
        return cardinality;
    }

    /**
     * Get the details of this term.
     * Cache the result to avoid calling the API again if this method is called
     * multiple times.
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
        details = RegexSolverApiWrapper.getInstance().analyzeDetails(this);
        return details;
    }

    /**
     * Get the GraphViz DOT representation of this term.
     * Cache the result to avoid calling the API again if this method is called
     * multiple times.
     *
     * @return A DOT language string describing the automaton for this term.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public String getDot() throws IOException, ApiError {
        if (dot != null) {
            return dot;
        }
        dot = RegexSolverApiWrapper.getInstance()
                .analyzeDot(this);
        return dot;
    }

    /**
     * Return the Fast Automaton Internal Representation (FAIR).
     *
     * @return The FAIR.
     */
    @JsonIgnore
    public String getFair() throws IOException, ApiError {
        return null;
    }

    /**
     * Get the length bounds of this term.
     * Cache the result to avoid calling the API again if this method is called
     * multiple times.
     *
     * @return A `Length` object with the minimum and maximum string length matched
     *         by this term.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Length getLength() throws IOException, ApiError {
        if (length != null) {
            return length;
        } else if (details != null) {
            return details.getLength();
        }

        length = RegexSolverApiWrapper.getInstance()
                .analyzeLength(this);
        return length;
    }

    /**
     * Return the regular expression pattern.
     * 
     * If the term is not a regex the pattern will be resolved.
     * Cache the result to avoid calling the API again if this method is called
     * multiple times.
     *
     * @return The regular expression pattern.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public String getPattern() throws IOException, ApiError {
        if (pattern != null) {
            return pattern;
        }

        pattern = RegexSolverApiWrapper.getInstance()
                .analyzePattern(this);
        return pattern;
    }

    /**
     * Check whether this term matches no string.
     * Cache the result to avoid calling the API again if this method is called
     * multiple times.
     *
     * @return true if the term is empty, false otherwise.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public boolean isEmpty() throws IOException, ApiError {
        if (empty != null) {
            return empty;
        } else if (details != null) {
            return details.isEmpty();
        }

        empty = RegexSolverApiWrapper.getInstance()
                .analyzeEmpty(this);
        return empty;
    }

    /**
     * Check whether this term matches only the empty string.
     * Cache the result to avoid calling the API again if this method is called
     * multiple times.
     *
     * @return true if the term only matches the empty string, false otherwise.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public boolean isEmptyString() throws IOException, ApiError {
        if (emptyString != null) {
            return emptyString;
        }

        emptyString = RegexSolverApiWrapper.getInstance()
                .analyzeEmptyString(this);
        return emptyString;
    }

    /**
     * Check whether this term matches all possible strings.
     * Cache the result to avoid calling the API again if this method is called
     * multiple times.
     *
     * @return true if the term matches all possible strings, false otherwise.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public boolean isTotal() throws IOException, ApiError {
        if (total != null) {
            return total;
        } else if (details != null) {
            return details.isTotal();
        }

        total = RegexSolverApiWrapper.getInstance()
                .analyzeTotal(this);
        return total;
    }

    /**
     * Check if is a subset of the given term.
     *
     * @param opts Execution options.
     * @param term The term to check if is the superset of this.
     * @return true if this is a subset, false otherwise.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public boolean subset(OperationOptions opts, Term term) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .analyzeSubset(new MultiTermsRequest(getArgs(term), loadRequestOptions(opts)));
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
    public boolean subset(Term term) throws IOException, ApiError {
        return subset(null, term);
    }

    // Compute

    @JsonIgnore
    private List<Term> getArgs(Term... terms) {
        ArrayList<Term> args = new ArrayList<>();
        args.add(this);
        args.addAll(List.of(terms));
        return args;
    }

    /**
     * Compute the concat with the given terms and return the resulting term.
     *
     * @param opts  Execution options.
     * @param terms The terms to compute an concat with.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term concat(OperationOptions opts, Term... terms) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .computeConcat(new MultiTermsRequest(getArgs(terms), loadRequestOptions(opts)));
    }

    /**
     * Compute the concat with the given terms and return the resulting term.
     *
     * @param terms The terms to compute an concat with.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term concat(Term... terms) throws IOException, ApiError {
        return concat(null, terms);
    }

    /**
     * Compute the difference with the given term and return the resulting term.
     *
     * @param opts Execution options.
     * @param term The term to subtract.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term difference(OperationOptions opts, Term term) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .computeDifference(new MultiTermsRequest(getArgs(term), loadRequestOptions(opts)));
    }

    /**
     * Compute the difference with the given term and return the resulting term.
     *
     * @param term The term to subtract.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term difference(Term term) throws IOException, ApiError {
        return difference(null, term);
    }

    /**
     * Compute the intersection with the given terms and return the resulting term.
     *
     * @param opts  Execution options.
     * @param terms The terms to compute an intersection with.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term intersection(OperationOptions opts, Term... terms) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .computeIntersection(new MultiTermsRequest(getArgs(terms), loadRequestOptions(opts)));
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
        return intersection(null, terms);
    }

    /**
     * Computes the repetition of the term between `min` and `max` times; if `max`
     * is `null`, the repetition is unbounded.
     *
     * @param opts Execution options.
     * @param min  The lower bound of the repetition.
     * @param max  The upper bound of the repetition, if `null` the repetition is
     *             unbounded.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term repeat(OperationOptions opts, int min, Integer max) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .computeRepeat(new RepeatRequest(this, min, max, loadRequestOptions(opts)));
    }

    /**
     * Computes the repetition of the term between `min` and `max` times; if `max`
     * is `null`, the repetition is unbounded.
     *
     * @param min The lower bound of the repetition.
     * @param max The upper bound of the repetition, if `null` the repetition is
     *            unbounded.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term repeat(int min, Integer max) throws IOException, ApiError {
        return repeat(null, min, max);
    }

    /**
     * Compute the union with the given terms and return the resulting term.
     *
     * @param opts  Execution options.
     * @param terms The terms to compute a union with.
     * @return The resulting term
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public Term union(OperationOptions opts, Term... terms) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .computeUnion(new MultiTermsRequest(getArgs(terms), loadRequestOptions(opts)));
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
        return union(null, terms);
    }

    // Generate

    /**
     * Generate the given number of unique strings matched by this term.
     *
     * @param opts  Execution options.
     * @param count The number of unique strings to generate.
     * @return A list of unique strings matched by this term.
     * @throws IOException In case of issues requesting the API server.
     * @throws ApiError    In case of error returned by the API.
     */
    @JsonIgnore
    public List<String> generateStrings(OperationOptions opts, int count) throws IOException, ApiError {
        return RegexSolverApiWrapper.getInstance()
                .generateStrings(new GenerateStringsRequest(this, count, loadRequestOptions(opts)));
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
        return generateStrings(null, count);
    }

    /**
     * Generate a string representation that can be parsed by
     * {@link #deserialize(String)}.
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
     * Parse a string representation of a {@link Term} produced by
     * {@link #serialize()}.
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
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
     * You can learn more about FAIR in our
     * <a href="https://docs.regexsolver.com/" target="_blank">documentation</a>.
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

        @JsonProperty("value")
        @Override
        public String getFair() {
            return getValue();
        }
    }

    /**
     * This term represents a regular expression.
     * <p>
     * You can learn more about regular expression in our
     * <a href="https://docs.regexsolver.com/" target="_blank">documentation</a>
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

        @JsonProperty("value")
        @Override
        public String getPattern() {
            return getValue();
        }
    }

    public static final class OperationOptions {
        private ResponseFormat responseFormat;
        private Integer executionTimeout;

        public static OperationOptions init() {
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
}
