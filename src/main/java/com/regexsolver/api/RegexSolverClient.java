package com.regexsolver.api;

import java.util.List;

/**
 * The Synchronous Client for RegexSolver.
 *
 * Provides blocking access to all RegexSolver API endpoints.
 */
public final class RegexSolverClient {

    private final AsyncRegexSolverClient asyncClient;

    private RegexSolverClient(AsyncRegexSolverClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final AsyncRegexSolverClient.Builder asyncBuilder =
            AsyncRegexSolverClient.builder();

        public Builder apiToken(String apiToken) {
            asyncBuilder.apiToken(apiToken);
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            asyncBuilder.baseUrl(baseUrl);
            return this;
        }

        public RegexSolverClient build() {
            return new RegexSolverClient(asyncBuilder.build());
        }
    }

    // --- ANALYZE OPERATIONS ---

    /**
     * Computes how many unique strings the term matches.
     *
     * @param term The term to analyze.
     * @return A Cardinality object representing either an exact Integer, a BigInteger, or Infinite cardinality.
     */
    public Cardinality getCardinality(Term term) {
        return asyncClient.getCardinality(term).join();
    }

    /**
     * Computes how many unique strings the term matches, with a timeout.
     *
     * @param term    The term to analyze.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A Cardinality object representing either an exact Integer, a BigInteger, or Infinite cardinality.
     */
    public Cardinality getCardinality(Term term, Integer timeout) {
        return asyncClient.getCardinality(term, timeout).join();
    }

    /**
     * Computes the minimum and maximum length of strings matched by the term.
     *
     * @param term The term to analyze.
     * @return A Length object containing min and max integers. Limits are null if unbounded or undefined.
     */
    public Length getLength(Term term) {
        return asyncClient.getLength(term).join();
    }

    /**
     * Computes the minimum and maximum length of strings matched by the term, with a timeout.
     *
     * @param term    The term to analyze.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A Length object containing min and max integers. Limits are null if unbounded or undefined.
     */
    public Length getLength(Term term, Integer timeout) {
        return asyncClient.getLength(term, timeout).join();
    }

    /**
     * Checks if the term matches no strings at all.
     *
     * @param term The term to analyze.
     * @return True if the language is completely empty, false otherwise.
     */
    public boolean isEmpty(Term term) {
        return asyncClient.isEmpty(term).join();
    }

    /**
     * Checks if the term matches no strings at all, with a timeout.
     *
     * @param term    The term to analyze.
     * @param timeout Timeout in milliseconds for the operation.
     * @return True if the language is completely empty, false otherwise.
     */
    public boolean isEmpty(Term term, Integer timeout) {
        return asyncClient.isEmpty(term, timeout).join();
    }

    /**
     * Checks if the term matches only the empty string.
     *
     * @param term The term to analyze.
     * @return True if the term strictly matches the empty string ("") and nothing else.
     */
    public boolean isEmptyString(Term term) {
        return asyncClient.isEmptyString(term).join();
    }

    /**
     * Checks if the term matches only the empty string, with a timeout.
     *
     * @param term    The term to analyze.
     * @param timeout Timeout in milliseconds for the operation.
     * @return True if the term strictly matches the empty string ("") and nothing else.
     */
    public boolean isEmptyString(Term term, Integer timeout) {
        return asyncClient.isEmptyString(term, timeout).join();
    }

    /**
     * Checks if the term matches all possible strings.
     *
     * @param term The term to analyze.
     * @return True if the term matches every possible string.
     */
    public boolean isTotal(Term term) {
        return asyncClient.isTotal(term).join();
    }

    /**
     * Checks if the term matches all possible strings, with a timeout.
     *
     * @param term    The term to analyze.
     * @param timeout Timeout in milliseconds for the operation.
     * @return True if the term matches every possible string.
     */
    public boolean isTotal(Term term, Integer timeout) {
        return asyncClient.isTotal(term, timeout).join();
    }

    /**
     * Returns a regular expression pattern that represents the term.
     *
     * @param term The term to extract the pattern from.
     * @return A valid regular expression string representing the language.
     */
    public String getPattern(Term term) {
        return asyncClient.getPattern(term).join();
    }

    /**
     * Returns a regular expression pattern that represents the term, with a timeout.
     *
     * @param term    The term to extract the pattern from.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A valid regular expression string representing the language.
     */
    public String getPattern(Term term, Integer timeout) {
        return asyncClient.getPattern(term, timeout).join();
    }

    /**
     * Builds a Graphviz DOT representation of the term's automaton.
     *
     * @param term The term to visualize.
     * @return The raw DOT syntax for Graphviz compilation.
     */
    public String getDot(Term term) {
        return asyncClient.getDot(term).join();
    }

    /**
     * Builds a Graphviz DOT representation of the term's automaton, with a timeout.
     *
     * @param term    The term to visualize.
     * @param timeout Timeout in milliseconds for the operation.
     * @return The raw DOT syntax for Graphviz compilation.
     */
    public String getDot(Term term, Integer timeout) {
        return asyncClient.getDot(term, timeout).join();
    }

    /**
     * Checks if the two terms accept exactly the same language.
     *
     * @param term1 The first term.
     * @param term2 The second term to compare against.
     * @return True if they are entirely equivalent, false otherwise.
     */
    public boolean equivalent(Term term1, Term term2) {
        return asyncClient.equivalent(term1, term2).join();
    }

    /**
     * Checks if the two terms accept exactly the same language, with a timeout.
     *
     * @param term1   The first term.
     * @param term2   The second term to compare against.
     * @param timeout Timeout in milliseconds for the operation.
     * @return True if they are entirely equivalent, false otherwise.
     */
    public boolean equivalent(Term term1, Term term2, Integer timeout) {
        return asyncClient.equivalent(term1, term2, timeout).join();
    }

    /**
     * Checks if the first term's language is a subset of the second term's language.
     *
     * @param subset   The term to test as the subset.
     * @param superset The term representing the entire set space.
     * @return True if every string matched by subset is also matched by superset.
     */
    public boolean subset(Term subset, Term superset) {
        return asyncClient.subset(subset, superset).join();
    }

    /**
     * Checks if the first term's language is a subset of the second term's language, with a timeout.
     *
     * @param subset   The term to test as the subset.
     * @param superset The term representing the entire set space.
     * @param timeout  Timeout in milliseconds for the operation.
     * @return True if every string matched by subset is also matched by superset.
     */
    public boolean subset(Term subset, Term superset, Integer timeout) {
        return asyncClient.subset(subset, superset, timeout).join();
    }

    // --- COMPUTE OPERATIONS ---

    /**
     * Concatenates the given terms sequentially.
     *
     * @param terms A list of terms to concatenate in order.
     * @return A newly computed concatenated term.
     */
    public Term concat(List<Term> terms) {
        return asyncClient.concat(terms).join();
    }

    /**
     * Concatenates the given terms sequentially, allowing for format and timeout specification.
     *
     * @param terms   A list of terms to concatenate in order.
     * @param format  The return format of the term (any, regex or fair).
     * @param timeout Timeout in milliseconds for the operation.
     * @return A newly computed concatenated term.
     */
    public Term concat(
        List<Term> terms,
        ResponseFormat format,
        Integer timeout
    ) {
        return asyncClient.concat(terms, format, timeout).join();
    }

    /**
     * Computes the intersection of the given terms.
     *
     * @param terms A list of terms to intersect.
     * @return A term representing only strings matched by ALL provided terms.
     */
    public Term intersection(List<Term> terms) {
        return asyncClient.intersection(terms).join();
    }

    /**
     * Computes the intersection of the given terms, allowing for format and timeout specification.
     *
     * @param terms   A list of terms to intersect.
     * @param format  The return format of the term (any, regex or fair).
     * @param timeout Timeout in milliseconds for the operation.
     * @return A term representing only strings matched by ALL provided terms.
     */
    public Term intersection(
        List<Term> terms,
        ResponseFormat format,
        Integer timeout
    ) {
        return asyncClient.intersection(terms, format, timeout).join();
    }

    /**
     * Computes the union of the given terms.
     *
     * @param terms A list of terms to combine.
     * @return A term representing strings matched by ANY of the provided terms.
     */
    public Term union(List<Term> terms) {
        return asyncClient.union(terms).join();
    }

    /**
     * Computes the union of the given terms, allowing for format and timeout specification.
     *
     * @param terms   A list of terms to combine.
     * @param format  The return format of the term (any, regex or fair).
     * @param timeout Timeout in milliseconds for the operation.
     * @return A term representing strings matched by ANY of the provided terms.
     */
    public Term union(
        List<Term> terms,
        ResponseFormat format,
        Integer timeout
    ) {
        return asyncClient.union(terms, format, timeout).join();
    }

    /**
     * Computes the difference between the two provided terms.
     *
     * @param base     The base language term to subtract from.
     * @param excluded The term whose language should be removed from the base.
     * @return A computed difference term.
     */
    public Term difference(Term base, Term excluded) {
        return asyncClient.difference(base, excluded).join();
    }

    /**
     * Computes the difference between the two provided terms, allowing for format and timeout specification.
     *
     * @param base     The base language term to subtract from.
     * @param excluded The term whose language should be removed from the base.
     * @param format   The return format of the term (any, regex or fair).
     * @param timeout  Timeout in milliseconds for the operation.
     * @return A computed difference term.
     */
    public Term difference(
        Term base,
        Term excluded,
        ResponseFormat format,
        Integer timeout
    ) {
        return asyncClient.difference(base, excluded, format, timeout).join();
    }

    /**
     * Computes the complement of the given term.
     *
     * @param term The term to complement.
     * @return The complemented term.
     */
    public Term complement(Term term) {
        return asyncClient.complement(term).join();
    }

    /**
     * Computes the complement of the given term, allowing for format and timeout specification.
     *
     * @param term    The term to complement.
     * @param format  The return format of the term (any, regex or fair).
     * @param timeout Timeout in milliseconds for the operation.
     * @return The complemented term.
     */
    public Term complement(Term term, ResponseFormat format, Integer timeout) {
        return asyncClient.complement(term, format, timeout).join();
    }

    /**
     * Repeats a term between a minimum and maximum number of times.
     *
     * @param term The term to repeat.
     * @param min  The inclusive lower bound of repetitions.
     * @param max  The inclusive upper bound. If null, repetitions are unbounded.
     * @return A computed repeated term.
     */
    public Term repeat(Term term, int min, Integer max) {
        return asyncClient.repeat(term, min, max).join();
    }

    /**
     * Repeats a term between a minimum and maximum number of times, allowing for format and timeout specification.
     *
     * @param term    The term to repeat.
     * @param min     The inclusive lower bound of repetitions.
     * @param max     The inclusive upper bound. If null, repetitions are unbounded.
     * @param format  The return format of the term (any, regex or fair).
     * @param timeout Timeout in milliseconds for the operation.
     * @return A computed repeated term.
     */
    public Term repeat(
        Term term,
        int min,
        Integer max,
        ResponseFormat format,
        Integer timeout
    ) {
        return asyncClient.repeat(term, min, max, format, timeout).join();
    }

    // --- GENERATE OPERATIONS ---

    /**
     * Generates up to {@code limit} distinct strings matched by the term, skipping the first {@code offset} strings.
     *
     * @param term   The term to sample generated strings from.
     * @param limit  The maximum number of unique strings to return.
     * @param offset Number of matched strings to skip before starting to collect the results. Used for pagination.
     * @return A list of strings that match the term.
     */
    public List<String> generateStrings(Term term, int limit, int offset) {
        return asyncClient.generateStrings(term, limit, offset).join();
    }

    /**
     * Generates up to {@code limit} distinct strings matched by the term, skipping the first {@code offset} strings, with a timeout.
     *
     * @param term    The term to sample generated strings from.
     * @param limit   The maximum number of unique strings to return.
     * @param offset  Number of matched strings to skip before starting to collect the results. Used for pagination.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A list of strings that match the term.
     */
    public List<String> generateStrings(
        Term term,
        int limit,
        int offset,
        Integer timeout
    ) {
        return asyncClient.generateStrings(term, limit, offset, timeout).join();
    }
}
