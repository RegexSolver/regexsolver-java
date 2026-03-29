package com.regexsolver.api;

import java.util.List;

/**
 * The Synchronous Client for RegexSolver.
 *
 * Provides blocking access to all RegexSolver API endpoints.
 */
public final class RegexSolverClient {

    private final AsyncRegexSolverClient asyncClient;

    private RegexSolverClient(Builder builder) {
        this.asyncClient = builder.asyncBuilder.build();
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
            return new RegexSolverClient(this);
        }
    }

    // --- ANALYZE OPERATIONS ---

    /**
     * Computes how many unique strings the term matches.
     *
     * @param term The term to analyze.
     * @return Cardinality object representing either an exact Integer, a BigInteger, or Infinite cardinality.
     */
    public Cardinality getCardinality(Term term) {
        return getCardinality(term, (OperationOptions) null);
    }

    /**
     * Computes how many unique strings the term matches.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return Cardinality object representing either an exact Integer, a BigInteger, or Infinite cardinality.
     */
    public Cardinality getCardinality(Term term, OperationOptions options) {
        return asyncClient.getCardinality(term, options).join();
    }

    /**
     * Computes the minimum and maximum length of strings matched by the term.
     *
     * @param term The term to analyze.
     * @return Length object with `min` and `max` integers. Limits are null if unbounded or undefined.
     */
    public Length getLength(Term term) {
        return getLength(term, (OperationOptions) null);
    }

    /**
     * Computes the minimum and maximum length of strings matched by the term.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return Length object with `min` and `max` integers. Limits are null if unbounded or undefined.
     */
    public Length getLength(Term term, OperationOptions options) {
        return asyncClient.getLength(term, options).join();
    }

    /**
     * Checks if the term matches no strings at all.
     *
     * @param term The term to analyze.
     * @return true if the language is completely empty, false otherwise.
     */
    public boolean isEmpty(Term term) {
        return isEmpty(term, (OperationOptions) null);
    }

    /**
     * Checks if the term matches no strings at all.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return true if the language is completely empty, false otherwise.
     */
    public boolean isEmpty(Term term, OperationOptions options) {
        return asyncClient.isEmpty(term, options).join();
    }

    /**
     * Checks if the term matches only the empty string.
     *
     * @param term The term to analyze.
     * @return true if the term strictly matches the empty string ("") and nothing else.
     */
    public boolean isEmptyString(Term term) {
        return isEmptyString(term, (OperationOptions) null);
    }

    /**
     * Checks if the term matches only the empty string.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return true if the term strictly matches the empty string ("") and nothing else.
     */
    public boolean isEmptyString(Term term, OperationOptions options) {
        return asyncClient.isEmptyString(term, options).join();
    }

    /**
     * Checks if the term matches all possible strings.
     *
     * @param term The term to analyze.
     * @return true if the term matches every possible string.
     */
    public boolean isTotal(Term term) {
        return isTotal(term, (OperationOptions) null);
    }

    /**
     * Checks if the term matches all possible strings.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return true if the term matches every possible string.
     */
    public boolean isTotal(Term term, OperationOptions options) {
        return asyncClient.isTotal(term, options).join();
    }

    /**
     * Returns a regular expression pattern that represents the term.
     *
     * @param term The term to extract the pattern from.
     * @return A valid regular expression string representing the language.
     */
    public String getPattern(Term term) {
        return getPattern(term, (OperationOptions) null);
    }

    /**
     * Returns a regular expression pattern that represents the term.
     *
     * @param term    The term to extract the pattern from.
     * @param options Options for the operation.
     * @return A valid regular expression string representing the language.
     */
    public String getPattern(Term term, OperationOptions options) {
        return asyncClient.getPattern(term, options).join();
    }

    /**
     * Builds a Graphviz DOT representation of the term's automaton.
     *
     * @param term The term to visualize.
     * @return The raw DOT syntax for Graphviz compilation.
     */
    public String getDot(Term term) {
        return getDot(term, (OperationOptions) null);
    }

    /**
     * Builds a Graphviz DOT representation of the term's automaton.
     *
     * @param term    The term to visualize.
     * @param options Options for the operation.
     * @return The raw DOT syntax for Graphviz compilation.
     */
    public String getDot(Term term, OperationOptions options) {
        return asyncClient.getDot(term, options).join();
    }

    /**
     * Checks if the two terms accept exactly the same language.
     *
     * @param term1 The first term.
     * @param term2 The second term to compare against.
     * @return true if they are entirely equivalent, false otherwise.
     */
    public boolean equivalent(Term term1, Term term2) {
        return equivalent(term1, term2, (OperationOptions) null);
    }

    /**
     * Checks if the two terms accept exactly the same language.
     *
     * @param term1   The first term.
     * @param term2   The second term to compare against.
     * @param options Options for the operation.
     * @return true if they are entirely equivalent, false otherwise.
     */
    public boolean equivalent(Term term1, Term term2, OperationOptions options) {
        return asyncClient.equivalent(term1, term2, options).join();
    }

    /**
     * Checks if the first term's language is a subset of the second term's language.
     *
     * @param subset   The term to test as the subset.
     * @param superset The term representing the entire set space.
     * @return true if every string matched by subset is also matched by superset.
     */
    public boolean subset(Term subset, Term superset) {
        return subset(subset, superset, (OperationOptions) null);
    }

    /**
     * Checks if the first term's language is a subset of the second term's language.
     *
     * @param subset   The term to test as the subset.
     * @param superset The term representing the entire set space.
     * @param options  Options for the operation.
     * @return true if every string matched by subset is also matched by superset.
     */
    public boolean subset(
        Term subset,
        Term superset,
        OperationOptions options
    ) {
        return asyncClient.subset(subset, superset, options).join();
    }

    // --- COMPUTE OPERATIONS ---

    /**
     * Concatenates the given terms sequentially.
     *
     * @param terms Variadic terms to concatenate in order.
     * @return A newly computed concatenated term.
     */
    public Term concat(Term... terms) {
        return asyncClient.concat(terms).join();
    }

    /**
     * Concatenates the given terms sequentially.
     *
     * @param terms A list of terms to concatenate in order.
     * @return A newly computed concatenated term.
     */
    public Term concat(List<Term> terms) {
        return concat(terms, (OperationOptions) null);
    }

    /**
     * Concatenates the given terms sequentially, allowing for options specification.
     *
     * @param terms   A list of terms to concatenate in order.
     * @param options Options for the operation.
     * @return A newly computed concatenated term.
     */
    public Term concat(List<Term> terms, OperationOptions options) {
        return asyncClient.concat(terms, options).join();
    }

    /**
     * Computes the intersection of the given terms.
     *
     * @param terms Variadic terms to intersect.
     * @return A term representing only strings matched by ALL provided terms.
     */
    public Term intersection(Term... terms) {
        return asyncClient.intersection(terms).join();
    }

    /**
     * Computes the intersection of the given terms.
     *
     * @param terms A list of terms to intersect.
     * @return A term representing only strings matched by ALL provided terms.
     */
    public Term intersection(List<Term> terms) {
        return intersection(terms, (OperationOptions) null);
    }

    /**
     * Computes the intersection of the given terms, allowing for options specification.
     *
     * @param terms   A list of terms to intersect.
     * @param options Options for the operation.
     * @return A term representing only strings matched by ALL provided terms.
     */
    public Term intersection(List<Term> terms, OperationOptions options) {
        return asyncClient.intersection(terms, options).join();
    }

    /**
     * Computes the union of the given terms.
     *
     * @param terms Variadic terms to combine.
     * @return A term representing strings matched by ANY of the provided terms.
     */
    public Term union(Term... terms) {
        return asyncClient.union(terms).join();
    }

    /**
     * Computes the union of the given terms.
     *
     * @param terms A list of terms to combine.
     * @return A term representing strings matched by ANY of the provided terms.
     */
    public Term union(List<Term> terms) {
        return union(terms, (OperationOptions) null);
    }

    /**
     * Computes the union of the given terms, allowing for options specification.
     *
     * @param terms   A list of terms to combine.
     * @param options Options for the operation.
     * @return A term representing strings matched by ANY of the provided terms.
     */
    public Term union(List<Term> terms, OperationOptions options) {
        return asyncClient.union(terms, options).join();
    }

    /**
     * Computes the difference between the two provided terms.
     *
     * @param base     The base language term to subtract from.
     * @param excluded The term whose language should be removed from the base.
     * @return A computed difference term.
     */
    public Term difference(Term base, Term excluded) {
        return difference(base, excluded, (OperationOptions) null);
    }

    /**
     * Computes the difference between the two provided terms.
     *
     * @param base     The base language term to subtract from.
     * @param excluded The term whose language should be removed from the base.
     * @param options  Options for the operation.
     * @return A computed difference term.
     */
    public Term difference(Term base, Term excluded, OperationOptions options) {
        return asyncClient.difference(base, excluded, options).join();
    }

    /**
     * Computes the complement of the given term.
     *
     * @param term The term to complement.
     * @return The complemented term.
     */
    public Term complement(Term term) {
        return complement(term, (OperationOptions) null);
    }

    /**
     * Computes the complement of the given term.
     *
     * @param term    The term to complement.
     * @param options Options for the operation.
     * @return The complemented term.
     */
    public Term complement(Term term, OperationOptions options) {
        return asyncClient.complement(term, options).join();
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
        return repeat(term, min, max, (OperationOptions) null);
    }

    /**
     * Repeats a term between a minimum and maximum number of times.
     *
     * @param term    The term to repeat.
     * @param min     The inclusive lower bound of repetitions.
     * @param max     The inclusive upper bound. If null, repetitions are unbounded.
     * @param options Options for the operation.
     * @return A computed repeated term.
     */
    public Term repeat(
        Term term,
        int min,
        Integer max,
        OperationOptions options
    ) {
        return asyncClient.repeat(term, min, max, options).join();
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
        return generateStrings(term, limit, offset, (OperationOptions) null);
    }

    /**
     * Generates up to {@code limit} distinct strings matched by the term, skipping the first {@code offset} strings.
     *
     * @param term    The term to sample generated strings from.
     * @param limit   The maximum number of unique strings to return.
     * @param offset  Number of matched strings to skip before starting to collect the results. Used for pagination.
     * @param options Options for the operation.
     * @return A list of strings that match the term.
     */
    public List<String> generateStrings(
        Term term,
        int limit,
        int offset,
        OperationOptions options
    ) {
        return asyncClient.generateStrings(term, limit, offset, options).join();
    }
}
