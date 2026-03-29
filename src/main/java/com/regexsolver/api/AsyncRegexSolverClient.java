package com.regexsolver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regexsolver.api.exceptions.*;
import com.regexsolver.api.generated.ApiClient;
import com.regexsolver.api.generated.ApiException;
import com.regexsolver.api.generated.api.AnalyzeApi;
import com.regexsolver.api.generated.api.ComputeApi;
import com.regexsolver.api.generated.api.GenerateApi;
import com.regexsolver.api.generated.model.*;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The Asynchronous Client for RegexSolver.
 *
 * Provides non-blocking access to all RegexSolver API endpoints.
 */
public final class AsyncRegexSolverClient {

    private static final String VERSION = "1.1.0";
    private final String apiToken;
    private final String baseUrl;
    private final RateLimiter rateLimiter;
    private final AnalyzeApi analyzeApi;
    private final ComputeApi computeApi;
    private final GenerateApi generateApi;
    private final ObjectMapper objectMapper;

    private AsyncRegexSolverClient(Builder builder) {
        this.apiToken = builder.apiToken;
        this.baseUrl = builder.baseUrl;
        this.rateLimiter = RateLimiter.getInstance(this.apiToken);

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(this.baseUrl);
        apiClient.setRequestInterceptor(requestBuilder -> {
            requestBuilder.header(
                "User-Agent",
                "RegexSolver Java / " + VERSION
            );
            requestBuilder.header("Authorization", "Bearer " + this.apiToken);
        });

        this.analyzeApi = new AnalyzeApi(apiClient);
        this.computeApi = new ComputeApi(apiClient);
        this.generateApi = new GenerateApi(apiClient);
        this.objectMapper = apiClient.getObjectMapper();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String apiToken;
        private String baseUrl = "https://api.regexsolver.com/v1";

        public Builder apiToken(String apiToken) {
            this.apiToken = apiToken;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public AsyncRegexSolverClient build() {
            if (apiToken == null || apiToken.isEmpty()) {
                throw new IllegalArgumentException("apiToken is required");
            }
            return new AsyncRegexSolverClient(this);
        }
    }

    // --- INTERNAL HELPERS ---

    private RequestOptionsDto buildOptions(
        Integer timeout,
        ResponseFormat format
    ) {
        RequestOptionsDto options = new RequestOptionsDto().schemaVersion(1);
        if (timeout != null) {
            options.execution(new ExecutionOptionsDto().timeout(timeout));
        }
        if (format != null) {
            options.response(new ResponseOptionsDto().format(format.toDto()));
        }
        return options;
    }

    private <T> CompletableFuture<T> executeWithRetry(
        Supplier<CompletableFuture<T>> apiCall
    ) {
        return executeWithRetry(apiCall, 0);
    }

    private <T> CompletableFuture<T> executeWithRetry(
        Supplier<CompletableFuture<T>> apiCall,
        int attempt
    ) {
        return rateLimiter
            .waitIfNecessary()
            .thenCompose(v -> apiCall.get())
            .exceptionallyCompose(ex -> {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                if (cause instanceof ApiException) {
                    ApiException apiEx = (ApiException) cause;
                    if (apiEx.getCode() == 429 && attempt < 5) {
                        double retryAfter = 1.0;
                        HttpHeaders headers = apiEx.getResponseHeaders();
                        if (headers != null) {
                            retryAfter = headers
                                .firstValue("Retry-After")
                                .map(Double::parseDouble)
                                .orElse(1.0);
                        }
                        rateLimiter.trigger(retryAfter);
                        return executeWithRetry(apiCall, attempt + 1);
                    }
                    throw mapException(apiEx);
                }
                if (
                    cause instanceof RuntimeException
                ) throw (RuntimeException) cause;
                throw new RuntimeException(cause);
            });
    }

    private RegexSolverException mapException(ApiException ex) {
        int code = ex.getCode();
        String body = ex.getResponseBody();
        String message = ex.getMessage();
        String errorCode = "UnknownError";

        try {
            ErrorResponseDto errorResponse = objectMapper.readValue(
                body,
                ErrorResponseDto.class
            );
            if (errorResponse.getError() != null) message =
                errorResponse.getError();
            if (errorResponse.getErrorCode() != null) errorCode =
                errorResponse.getErrorCode();
        } catch (Exception ignored) {}

        switch (code) {
            case 400:
                if (
                    "InvalidJson".equals(errorCode)
                ) return new InvalidJsonException(
                    message,
                    code,
                    errorCode,
                    body
                );
                if (
                    "TooManyTerms".equals(errorCode)
                ) return new TooManyTermsException(
                    message,
                    code,
                    errorCode,
                    body
                );
                if (
                    "TimeoutTooLarge".equals(errorCode)
                ) return new TimeoutTooLargeException(
                    message,
                    code,
                    errorCode,
                    body
                );
                if (
                    "TimeoutExceeded".equals(errorCode)
                ) return new TimeoutExceededException(
                    message,
                    code,
                    errorCode,
                    body
                );
                if (
                    "InvalidNumberOfStringsToGenerate".equals(errorCode)
                ) return new InvalidNumberOfStringsToGenerateException(
                    message,
                    code,
                    errorCode,
                    body
                );
                return new BadRequestException(message, code, errorCode, body);
            case 401:
                if (
                    "MissingOrMalformedToken".equals(errorCode)
                ) return new MissingOrMalformedTokenException(
                    message,
                    code,
                    errorCode,
                    body
                );
                if (
                    "InvalidToken".equals(errorCode)
                ) return new InvalidTokenException(
                    message,
                    code,
                    errorCode,
                    body
                );
                return new UnauthorizedException(
                    message,
                    code,
                    errorCode,
                    body
                );
            case 403:
                if (
                    "QuotaExceeded".equals(errorCode)
                ) return new QuotaExceededException(
                    message,
                    code,
                    errorCode,
                    body
                );
                return new ForbiddenException(message, code, errorCode, body);
            case 404:
                return new NotFoundException(message, code, errorCode, body);
            case 429:
                return new TooManyRequestsException(
                    "Max retries exceeded for 429 Too Many Requests.",
                    code,
                    errorCode,
                    body
                );
            case 500:
                return new InternalServerException(
                    message,
                    code,
                    errorCode,
                    body
                );
            default:
                return new com.regexsolver.api.exceptions.ApiException(
                    message,
                    code,
                    errorCode,
                    body
                );
        }
    }

    // --- ANALYZE OPERATIONS ---

    /**
     * Computes how many unique strings the term matches asynchronously.
     *
     * @param term The term to analyze.
     * @return A CompletableFuture containing a Cardinality object representing either an exact Integer, a BigInteger, or Infinite cardinality.
     */
    public CompletableFuture<Cardinality> getCardinality(Term term) {
        return getCardinality(term, null);
    }

    /**
     * Computes how many unique strings the term matches asynchronously.
     *
     * @param term    The term to analyze.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing a Cardinality object representing either an exact Integer, a BigInteger, or Infinite cardinality.
     */
    public CompletableFuture<Cardinality> getCardinality(
        Term term,
        Integer timeout
    ) {
        if (term.getCachedCardinality() != null) {
            return CompletableFuture.completedFuture(
                term.getCachedCardinality()
            );
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(timeout, null));
        return executeWithRetry(() ->
            analyzeApi.cardinality(request)
        ).thenApply(resp -> {
            Cardinality card = Cardinality.fromDto(resp.getData());
            term.setCachedCardinality(card);
            return card;
        });
    }

    /**
     * Computes the minimum and maximum length of strings matched by the term asynchronously.
     *
     * @param term The term to analyze.
     * @return A CompletableFuture containing a Length object with `min` and `max` integers. Limits are null if unbounded or undefined.
     */
    public CompletableFuture<Length> getLength(Term term) {
        return getLength(term, null);
    }

    /**
     * Computes the minimum and maximum length of strings matched by the term asynchronously.
     *
     * @param term    The term to analyze.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing a Length object with `min` and `max` integers. Limits are null if unbounded or undefined.
     */
    public CompletableFuture<Length> getLength(Term term, Integer timeout) {
        if (term.getCachedLength() != null) {
            return CompletableFuture.completedFuture(term.getCachedLength());
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(timeout, null));
        return executeWithRetry(() -> analyzeApi.length(request)).thenApply(
            resp -> {
                Length len = Length.fromDto(resp.getData());
                term.setCachedLength(len);
                return len;
            }
        );
    }

    /**
     * Checks if the term matches no strings at all asynchronously.
     *
     * @param term The term to analyze.
     * @return A CompletableFuture containing true if the language is completely empty, false otherwise.
     */
    public CompletableFuture<Boolean> isEmpty(Term term) {
        return isEmpty(term, null);
    }

    /**
     * Checks if the term matches no strings at all asynchronously.
     *
     * @param term    The term to analyze.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing true if the language is completely empty, false otherwise.
     */
    public CompletableFuture<Boolean> isEmpty(Term term, Integer timeout) {
        if (term.getCachedEmpty() != null) {
            return CompletableFuture.completedFuture(term.getCachedEmpty());
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(timeout, null));
        return executeWithRetry(() -> analyzeApi.empty(request)).thenApply(
            resp -> {
                boolean val = resp.getData().getValue();
                term.setCachedEmpty(val);
                if (val) {
                    term.setCachedCardinality(new Cardinality.Integer(0));
                    term.setCachedLength(new Length(null, null));
                }
                return val;
            }
        );
    }

    /**
     * Checks if the term matches only the empty string asynchronously.
     *
     * @param term The term to analyze.
     * @return A CompletableFuture containing true if the term strictly matches the empty string ("") and nothing else.
     */
    public CompletableFuture<Boolean> isEmptyString(Term term) {
        return isEmptyString(term, null);
    }

    /**
     * Checks if the term matches only the empty string asynchronously.
     *
     * @param term    The term to analyze.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing true if the term strictly matches the empty string ("") and nothing else.
     */
    public CompletableFuture<Boolean> isEmptyString(
        Term term,
        Integer timeout
    ) {
        if (term.getCachedEmptyString() != null) {
            return CompletableFuture.completedFuture(
                term.getCachedEmptyString()
            );
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(timeout, null));
        return executeWithRetry(() ->
            analyzeApi.emptyString(request)
        ).thenApply(resp -> {
            boolean val = resp.getData().getValue();
            term.setCachedEmptyString(val);

            if (val) {
                term.setCachedCardinality(new Cardinality.Integer(1));
                term.setCachedLength(new Length(0, 0));
            }
            return val;
        });
    }

    /**
     * Checks if the term matches all possible strings asynchronously.
     *
     * @param term The term to analyze.
     * @return A CompletableFuture containing true if the term matches every possible string.
     */
    public CompletableFuture<Boolean> isTotal(Term term) {
        return isTotal(term, null);
    }

    /**
     * Checks if the term matches all possible strings asynchronously.
     *
     * @param term    The term to analyze.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing true if the term matches every possible string.
     */
    public CompletableFuture<Boolean> isTotal(Term term, Integer timeout) {
        if (term.getCachedTotal() != null) {
            return CompletableFuture.completedFuture(term.getCachedTotal());
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(timeout, null));
        return executeWithRetry(() -> analyzeApi.total(request)).thenApply(
            resp -> {
                boolean val = resp.getData().getValue();
                term.setCachedTotal(val);

                if (val) {
                    term.setCachedCardinality(new Cardinality.Infinite());
                    term.setCachedLength(new Length(0, null));
                }
                return val;
            }
        );
    }

    /**
     * Returns a regular expression pattern that represents the term asynchronously.
     *
     * @param term The term to extract the pattern from.
     * @return A CompletableFuture containing a valid regular expression string representing the language.
     */
    public CompletableFuture<String> getPattern(Term term) {
        return getPattern(term, null);
    }

    /**
     * Returns a regular expression pattern that represents the term asynchronously.
     *
     * @param term    The term to extract the pattern from.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing a valid regular expression string representing the language.
     */
    public CompletableFuture<String> getPattern(Term term, Integer timeout) {
        Optional<String> patternOpt = term.getPattern();
        if (patternOpt.isPresent()) {
            return CompletableFuture.completedFuture(patternOpt.get());
        }

        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(timeout, null));
        return executeWithRetry(() -> analyzeApi.pattern(request)).thenApply(
            resp -> {
                String val = resp.getData().getValue();
                term.setCachedPattern(val);
                return val;
            }
        );
    }

    /**
     * Builds a Graphviz DOT representation of the term's automaton asynchronously.
     *
     * @param term The term to visualize.
     * @return A CompletableFuture containing the raw DOT syntax for Graphviz compilation.
     */
    public CompletableFuture<String> getDot(Term term) {
        return getDot(term, null);
    }

    /**
     * Builds a Graphviz DOT representation of the term's automaton asynchronously.
     *
     * @param term    The term to visualize.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing the raw DOT syntax for Graphviz compilation.
     */
    public CompletableFuture<String> getDot(Term term, Integer timeout) {
        if (term.getCachedDot() != null) {
            return CompletableFuture.completedFuture(term.getCachedDot());
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(timeout, null));
        return executeWithRetry(() -> analyzeApi.dot(request)).thenApply(
            resp -> {
                String val = resp.getData().getValue();
                term.setCachedDot(val);
                return val;
            }
        );
    }

    /**
     * Checks if the two terms accept exactly the same language asynchronously.
     *
     * @param term1 The first term.
     * @param term2 The second term to compare against.
     * @return A CompletableFuture containing true if they are entirely equivalent, false otherwise.
     */
    public CompletableFuture<Boolean> equivalent(Term term1, Term term2) {
        return equivalent(term1, term2, null);
    }

    /**
     * Checks if the two terms accept exactly the same language asynchronously.
     *
     * @param term1   The first term.
     * @param term2   The second term to compare against.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing true if they are entirely equivalent, false otherwise.
     */
    public CompletableFuture<Boolean> equivalent(
        Term term1,
        Term term2,
        Integer timeout
    ) {
        TwoTermsRequestDto request = new TwoTermsRequestDto()
            .addTermsItem(term1.toDto())
            .addTermsItem(term2.toDto())
            .options(buildOptions(timeout, null));
        return executeWithRetry(() -> analyzeApi.equivalent(request)).thenApply(
            resp -> resp.getData().getValue()
        );
    }

    /**
     * Checks if the first term's language is a subset of the second term's language asynchronously.
     *
     * @param subset   The term to test as the subset.
     * @param superset The term representing the entire set space.
     * @return A CompletableFuture containing true if every string matched by subset is also matched by superset.
     */
    public CompletableFuture<Boolean> subset(Term subset, Term superset) {
        return subset(subset, superset, null);
    }

    /**
     * Checks if the first term's language is a subset of the second term's language asynchronously.
     *
     * @param subset   The term to test as the subset.
     * @param superset The term representing the entire set space.
     * @param timeout  Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing true if every string matched by subset is also matched by superset.
     */
    public CompletableFuture<Boolean> subset(
        Term subset,
        Term superset,
        Integer timeout
    ) {
        TwoTermsRequestDto request = new TwoTermsRequestDto()
            .addTermsItem(subset.toDto())
            .addTermsItem(superset.toDto())
            .options(buildOptions(timeout, null));
        return executeWithRetry(() -> analyzeApi.subset(request)).thenApply(
            resp -> resp.getData().getValue()
        );
    }

    // --- COMPUTE OPERATIONS ---

    /**
     * Concatenates the given terms sequentially asynchronously.
     *
     * @param terms A dynamic list of terms to concatenate in order.
     * @return A CompletableFuture containing a newly computed concatenated term.
     */
    public CompletableFuture<Term> concat(List<Term> terms) {
        return concat(terms, ResponseFormat.ANY, null);
    }

    /**
     * Concatenates the given terms sequentially asynchronously.
     *
     * @param terms   A dynamic list of terms to concatenate in order.
     * @param format  The return format of the term (any, regex or fair).
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing a newly computed concatenated term.
     */
    public CompletableFuture<Term> concat(
        List<Term> terms,
        ResponseFormat format,
        Integer timeout
    ) {
        MultiTermsRequestDto request = new MultiTermsRequestDto()
            .terms(terms.stream().map(Term::toDto).collect(Collectors.toList()))
            .options(buildOptions(timeout, format));
        return executeWithRetry(() -> computeApi.concat(request)).thenApply(
            resp -> Term.fromDto(resp.getData())
        );
    }

    /**
     * Computes the intersection of the given terms asynchronously.
     *
     * @param terms A dynamic list of terms to intersect.
     * @return A CompletableFuture containing a term representing only strings matched by ALL provided terms.
     */
    public CompletableFuture<Term> intersection(List<Term> terms) {
        return intersection(terms, ResponseFormat.ANY, null);
    }

    /**
     * Computes the intersection of the given terms asynchronously.
     *
     * @param terms   A dynamic list of terms to intersect.
     * @param format  The return format of the term (any, regex or fair).
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing a term representing only strings matched by ALL provided terms.
     */
    public CompletableFuture<Term> intersection(
        List<Term> terms,
        ResponseFormat format,
        Integer timeout
    ) {
        MultiTermsRequestDto request = new MultiTermsRequestDto()
            .terms(terms.stream().map(Term::toDto).collect(Collectors.toList()))
            .options(buildOptions(timeout, format));
        return executeWithRetry(() ->
            computeApi.intersection(request)
        ).thenApply(resp -> Term.fromDto(resp.getData()));
    }

    /**
     * Computes the union of the given terms asynchronously.
     *
     * @param terms A dynamic list of terms to combine.
     * @return A CompletableFuture containing a term representing strings matched by ANY of the provided terms.
     */
    public CompletableFuture<Term> union(List<Term> terms) {
        return union(terms, ResponseFormat.ANY, null);
    }

    /**
     * Computes the union of the given terms asynchronously.
     *
     * @param terms   A dynamic list of terms to combine.
     * @param format  The return format of the term (any, regex or fair).
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing a term representing strings matched by ANY of the provided terms.
     */
    public CompletableFuture<Term> union(
        List<Term> terms,
        ResponseFormat format,
        Integer timeout
    ) {
        MultiTermsRequestDto request = new MultiTermsRequestDto()
            .terms(terms.stream().map(Term::toDto).collect(Collectors.toList()))
            .options(buildOptions(timeout, format));
        return executeWithRetry(() -> computeApi.union(request)).thenApply(
            resp -> Term.fromDto(resp.getData())
        );
    }

    /**
     * Computes the difference between the two provided terms asynchronously.
     *
     * @param base     The base language term to subtract from.
     * @param excluded The term whose language should be removed from the base.
     * @return A CompletableFuture containing a computed difference term.
     */
    public CompletableFuture<Term> difference(Term base, Term excluded) {
        return difference(base, excluded, ResponseFormat.ANY, null);
    }

    /**
     * Computes the difference between the two provided terms asynchronously.
     *
     * @param base     The base language term to subtract from.
     * @param excluded The term whose language should be removed from the base.
     * @param format   The return format of the term (any, regex or fair).
     * @param timeout  Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing a computed difference term.
     */
    public CompletableFuture<Term> difference(
        Term base,
        Term excluded,
        ResponseFormat format,
        Integer timeout
    ) {
        TwoTermsRequestDto request = new TwoTermsRequestDto()
            .addTermsItem(base.toDto())
            .addTermsItem(excluded.toDto())
            .options(buildOptions(timeout, format));
        return executeWithRetry(() -> computeApi.difference(request)).thenApply(
            resp -> Term.fromDto(resp.getData())
        );
    }

    /**
     * Computes the complement of the given term asynchronously.
     *
     * @param term The term to complement.
     * @return A CompletableFuture containing the complemented term.
     */
    public CompletableFuture<Term> complement(Term term) {
        return complement(term, ResponseFormat.ANY, null);
    }

    /**
     * Computes the complement of the given term asynchronously.
     *
     * @param term    The term to complement.
     * @param format  The return format of the term (any, regex or fair).
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing the complemented term.
     */
    public CompletableFuture<Term> complement(
        Term term,
        ResponseFormat format,
        Integer timeout
    ) {
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(timeout, format));
        return executeWithRetry(() -> computeApi.complement(request)).thenApply(
            resp -> Term.fromDto(resp.getData())
        );
    }

    /**
     * Repeats a term between a minimum and maximum number of times asynchronously.
     *
     * @param term The term to repeat.
     * @param min  The inclusive lower bound of repetitions.
     * @param max  The inclusive upper bound. If null, repetitions are unbounded.
     * @return A CompletableFuture containing a computed repeated term.
     */
    public CompletableFuture<Term> repeat(Term term, int min, Integer max) {
        return repeat(term, min, max, ResponseFormat.ANY, null);
    }

    /**
     * Repeats a term between a minimum and maximum number of times asynchronously.
     *
     * @param term    The term to repeat.
     * @param min     The inclusive lower bound of repetitions.
     * @param max     The inclusive upper bound. If null, repetitions are unbounded.
     * @param format  The return format of the term (any, regex or fair).
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing a computed repeated term.
     */
    public CompletableFuture<Term> repeat(
        Term term,
        int min,
        Integer max,
        ResponseFormat format,
        Integer timeout
    ) {
        RepeatRequestDto request = new RepeatRequestDto()
            .term(term.toDto())
            .min(min)
            .max(max)
            .options(buildOptions(timeout, format));
        return executeWithRetry(() -> computeApi.repeat(request)).thenApply(
            resp -> Term.fromDto(resp.getData())
        );
    }

    // --- GENERATE OPERATIONS ---

    /**
     * Generates up to {@code limit} distinct strings matched by the term, skipping the first {@code offset} strings asynchronously.
     *
     * @param term   The term to sample generated strings from.
     * @param limit  The maximum number of unique strings to return.
     * @param offset Number of matched strings to skip before starting to collect the results. Used for pagination.
     * @return A CompletableFuture containing a list of strings that match the term.
     */
    public CompletableFuture<List<String>> generateStrings(
        Term term,
        int limit,
        int offset
    ) {
        return generateStrings(term, limit, offset, null);
    }

    /**
     * Generates up to {@code limit} distinct strings matched by the term, skipping the first {@code offset} strings asynchronously.
     *
     * @param term    The term to sample generated strings from.
     * @param limit   The maximum number of unique strings to return.
     * @param offset  Number of matched strings to skip before starting to collect the results. Used for pagination.
     * @param timeout Timeout in milliseconds for the operation.
     * @return A CompletableFuture containing a list of strings that match the term.
     */
    public CompletableFuture<List<String>> generateStrings(
        Term term,
        int limit,
        int offset,
        Integer timeout
    ) {
        Term termToUse =
            term.getCachedStableTerm() != null
                ? term.getCachedStableTerm()
                : term;
        boolean returnStableTerm = term.getCachedStableTerm() == null;

        GenerateStringsRequestDto request = new GenerateStringsRequestDto()
            .term(termToUse.toDto())
            .limit(limit)
            .offset(offset)
            .returnStableTerm(returnStableTerm)
            .options(buildOptions(timeout, null));

        return executeWithRetry(() -> generateApi.strings(request)).thenApply(
            resp -> {
                GenerateStringsResponseDto data = resp.getData();
                if (data.getTerm() != null) {
                    term.setCachedStableTerm(Term.fromDto(data.getTerm()));
                }
                return data.getStrings().getValue();
            }
        );
    }
}
