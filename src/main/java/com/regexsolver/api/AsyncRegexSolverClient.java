package com.regexsolver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regexsolver.api.exceptions.*;
import com.regexsolver.api.generated.ApiClient;
import com.regexsolver.api.generated.ApiException;
import com.regexsolver.api.generated.api.AccountApi;
import com.regexsolver.api.generated.api.AnalyzeApi;
import com.regexsolver.api.generated.api.ComputeApi;
import com.regexsolver.api.generated.api.GenerateApi;
import com.regexsolver.api.generated.model.*;
import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The Asynchronous Client for RegexSolver.
 *
 * Provides non-blocking access to all RegexSolver API endpoints.
 */
public final class AsyncRegexSolverClient {

    private static final String VERSION = "1.1.0";

    // Retry policy for 429 responses: retry as long as the total wait stays
    // within the budget, adding full jitter on top of `Retry-After` so
    // concurrent waiters do not re-collide as a single burst. The values are
    // shared across all the official clients — change them together.
    private static final long RETRY_BUDGET_MS = 300_000;
    private static final double JITTER_BASE_S = 0.25;
    private static final double JITTER_CAP_S = 2.0;
    private static final double DEFAULT_RETRY_AFTER_S = 1.0;

    private final String apiToken;
    private final String baseUrl;
    private final RateLimiter rateLimiter;
    private final AccountApi accountApi;
    private final AnalyzeApi analyzeApi;
    private final ComputeApi computeApi;
    private final GenerateApi generateApi;
    private final ObjectMapper objectMapper;
    private final boolean autoBatch;
    private final Integer maxTermsPerRequest;
    private final AtomicReference<CompletableFuture<AccountLimits>> limitsFuture =
        new AtomicReference<>();
    private volatile Integer serverMaxTerms;

    private AsyncRegexSolverClient(Builder builder) {
        this.apiToken = builder.apiToken;
        this.baseUrl = builder.baseUrl;
        this.rateLimiter = RateLimiter.getInstance(this.apiToken);
        this.autoBatch = builder.autoBatch;
        this.maxTermsPerRequest = builder.maxTermsPerRequest;

        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri(this.baseUrl);
        apiClient.setRequestInterceptor(requestBuilder -> {
            requestBuilder.header(
                "User-Agent",
                "RegexSolver Java / " + VERSION
            );
            requestBuilder.header("Authorization", "Bearer " + this.apiToken);
        });

        this.accountApi = new AccountApi(apiClient);
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
        private boolean autoBatch = true;
        private Integer maxTermsPerRequest;

        public Builder apiToken(String apiToken) {
            this.apiToken = apiToken;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * When true (the default), calls to concat/intersection/union
         * carrying more terms than the account's per-request limit are
         * transparently split into several requests and folded back into one
         * result. Each constituent request counts against the monthly quota.
         *
         * @param autoBatch whether to enable auto-batching
         * @return this builder
         */
        public Builder autoBatch(boolean autoBatch) {
            this.autoBatch = autoBatch;
            return this;
        }

        /**
         * Upper bound (&gt;= 2) on the number of terms sent in a single
         * request, overriding the limit fetched from the API when smaller.
         *
         * @param maxTermsPerRequest the cap, or null to use the account limit
         * @return this builder
         */
        public Builder maxTermsPerRequest(Integer maxTermsPerRequest) {
            this.maxTermsPerRequest = maxTermsPerRequest;
            return this;
        }

        public AsyncRegexSolverClient build() {
            if (apiToken == null || apiToken.isEmpty()) {
                throw new IllegalArgumentException("apiToken is required");
            }
            if (maxTermsPerRequest != null && maxTermsPerRequest < 2) {
                throw new IllegalArgumentException(
                    "maxTermsPerRequest must be at least 2"
                );
            }
            return new AsyncRegexSolverClient(this);
        }
    }

    // --- INTERNAL HELPERS ---

    private RequestOptionsDto buildOptions(OperationOptions options) {
        RequestOptionsDto dto = new RequestOptionsDto().schemaVersion(1);
        if (options != null) {
            options
                .getExecutionTimeout()
                .ifPresent(timeout ->
                    dto.execution(new ExecutionOptionsDto().timeout(timeout))
                );

            Optional<ResponseFormat> responseFormat = options.getResponseFormat();
            Optional<Boolean> deterministic = options.getDeterministic();

            if (deterministic.isPresent() && responseFormat.isPresent()) {
                if (responseFormat.get() != ResponseFormat.FAIR) {
                    throw new IllegalArgumentException(
                        "deterministic can only be used with responseFormat=ResponseFormat.FAIR, got " +
                        responseFormat.get()
                    );
                }
            }

            if (responseFormat.isPresent() || deterministic.isPresent()) {
                ResponseOptionsDto responseOptions = new ResponseOptionsDto();
                responseFormat.ifPresent(format -> responseOptions.format(format.toDto()));
                deterministic.ifPresent(value -> {
                    responseOptions.fair(new FairResponseOptionsDto().deterministic(value));
                    if (responseFormat.isEmpty()) {
                        responseOptions.format(ResponseFormat.FAIR.toDto());
                    }
                });
                dto.response(responseOptions);
            }
        }
        return dto;
    }

    private <T> CompletableFuture<T> executeWithRetry(
        Supplier<CompletableFuture<T>> apiCall
    ) {
        return executeWithRetry(apiCall, 0, null);
    }

    private <T> CompletableFuture<T> executeWithRetry(
        Supplier<CompletableFuture<T>> apiCall,
        int attempt,
        Long firstFailureAtMillis
    ) {
        CompletableFuture<Void> gate = rateLimiter.waitIfNecessary();
        if (attempt > 0) {
            long jitterMillis = (long) (ThreadLocalRandom.current().nextDouble() *
                Math.min(JITTER_BASE_S * Math.pow(2, attempt), JITTER_CAP_S) *
                1000);
            gate = gate.thenCompose(v ->
                CompletableFuture.runAsync(
                    () -> {},
                    CompletableFuture.delayedExecutor(
                        Math.max(jitterMillis, 1),
                        java.util.concurrent.TimeUnit.MILLISECONDS
                    )
                )
            );
        }
        return exceptionallyCompose(
            gate.thenCompose(v -> apiCall.get()),
            ex -> {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                if (cause instanceof ApiException) {
                    ApiException apiEx = (ApiException) cause;
                    if (apiEx.getCode() == 429) {
                        double retryAfter = DEFAULT_RETRY_AFTER_S;
                        HttpHeaders headers = apiEx.getResponseHeaders();
                        if (headers != null) {
                            try {
                                retryAfter = headers
                                    .firstValue("Retry-After")
                                    .map(Double::parseDouble)
                                    .orElse(DEFAULT_RETRY_AFTER_S);
                            } catch (NumberFormatException ignored) {}
                        }
                        long now = System.currentTimeMillis();
                        long firstFailureAt = firstFailureAtMillis != null
                            ? firstFailureAtMillis
                            : now;
                        if (
                            now -
                                firstFailureAt +
                                (long) (retryAfter * 1000) <=
                            RETRY_BUDGET_MS
                        ) {
                            rateLimiter.trigger(retryAfter);
                            return executeWithRetry(
                                apiCall,
                                attempt + 1,
                                firstFailureAt
                            );
                        }
                    }
                    throw mapException(apiEx);
                }
                if (
                    cause instanceof RuntimeException
                ) throw (RuntimeException) cause;
                throw new RuntimeException(cause);
            }
        );
    }

    /**
     * `CompletableFuture.exceptionallyCompose` equivalent; that method is
     * only available from Java 12 onwards and this SDK targets Java 11.
     */
    private static <T> CompletableFuture<T> exceptionallyCompose(
        CompletableFuture<T> future,
        Function<Throwable, CompletionStage<T>> fallback
    ) {
        CompletableFuture<CompletionStage<T>> composed = future.handle(
            (value, ex) ->
                ex == null
                    ? CompletableFuture.completedFuture(value)
                    : fallback.apply(ex)
        );
        return composed.thenCompose(stage -> stage);
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
                    "TooFewTerms".equals(errorCode)
                ) return new TooFewTermsException(
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
                if (
                    "AutomatonTooManyStates".equals(errorCode)
                ) return new AutomatonTooManyStatesException(
                    message,
                    code,
                    errorCode,
                    body
                );
                if (
                    "RegexSyntaxError".equals(errorCode)
                ) return new RegexSyntaxException(
                    message,
                    code,
                    errorCode,
                    body
                );
                if (
                    "FairSyntaxError".equals(errorCode)
                ) return new FairSyntaxException(
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

    // --- ACCOUNT OPERATIONS ---

    /**
     * Fetches the plan limits applying to the account asynchronously.
     *
     * The call never consumes request quota (it is only rate-limited) and the
     * result is cached on the client, so calling it again is free. The cached
     * maxTermsCount also drives auto-batching.
     *
     * @return A CompletableFuture containing the five plan limits.
     */
    public CompletableFuture<AccountLimits> getAccountLimits() {
        CompletableFuture<AccountLimits> existing = limitsFuture.get();
        if (existing != null) {
            return existing;
        }
        CompletableFuture<AccountLimits> created = executeWithRetry(() ->
            accountApi.limits()
        ).thenApply(resp -> {
            AccountLimits limits = AccountLimits.fromDto(resp.getData());
            serverMaxTerms = (int) Math.min(
                limits.getMaxTermsCount(),
                Integer.MAX_VALUE
            );
            return limits;
        });
        if (!limitsFuture.compareAndSet(null, created)) {
            return limitsFuture.get();
        }
        created.whenComplete((limits, error) -> {
            if (error != null) {
                // Cleared on failure so a later call can retry the fetch.
                limitsFuture.compareAndSet(created, null);
            }
        });
        return created;
    }

    // --- AUTO-BATCHING ---

    /** The largest term count to send in one request, when known. */
    private Integer effectiveMaxTerms() {
        Integer serverMax = serverMaxTerms;
        if (maxTermsPerRequest != null) {
            return serverMax != null
                ? Math.min(maxTermsPerRequest, serverMax)
                : maxTermsPerRequest;
        }
        return serverMax;
    }

    /**
     * Run an n-ary operation (concat/intersection/union), transparently
     * splitting the terms into several requests when they exceed the
     * account's terms-per-request limit (auto-batching).
     */
    private CompletableFuture<Term> runNary(
        List<Term> terms,
        OperationOptions options,
        Function<MultiTermsRequestDto, CompletableFuture<TermDto>> op
    ) {
        Integer maxTerms = autoBatch ? effectiveMaxTerms() : null;
        if (maxTerms != null && terms.size() > maxTerms) {
            return fold(op, terms, options, maxTerms);
        }
        boolean limitWasKnown = maxTerms != null;
        return exceptionallyCompose(
            naryCall(op, terms, options, true),
            ex -> {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                if (
                    !autoBatch ||
                    limitWasKnown ||
                    !(cause instanceof TooManyTermsException)
                ) {
                    return CompletableFuture.failedFuture(cause);
                }
                return getAccountLimits()
                    .handle((limits, fetchError) ->
                        // A failed fetch falls back to surfacing the original
                        // TooManyTerms, never worse than without batching.
                        fetchError != null ? null : effectiveMaxTerms()
                    )
                    .thenCompose(newMax -> {
                        if (
                            newMax == null ||
                            newMax < 2 ||
                            terms.size() <= newMax
                        ) {
                            return CompletableFuture.failedFuture(cause);
                        }
                        return fold(op, terms, options, newMax);
                    });
            }
        );
    }

    private CompletableFuture<Term> naryCall(
        Function<MultiTermsRequestDto, CompletableFuture<TermDto>> op,
        List<Term> batch,
        OperationOptions options,
        boolean isFinal
    ) {
        // Intermediate results are fed straight back into the next request,
        // so only the final call carries the caller's response options;
        // executionTimeout bounds every constituent request.
        OperationOptions effective = isFinal
            ? options
            : intermediateOptions(options);
        MultiTermsRequestDto request = new MultiTermsRequestDto()
            .terms(batch.stream().map(Term::toDto).collect(Collectors.toList()))
            .options(buildOptions(effective));
        return op.apply(request).thenApply(Term::fromDto);
    }

    private static OperationOptions intermediateOptions(
        OperationOptions options
    ) {
        if (options == null) {
            return null;
        }
        return options
            .getExecutionTimeout()
            .map(timeout -> OperationOptions.builder().executionTimeout(timeout))
            .orElse(null);
    }

    /**
     * Left fold: combine the first {@code maxTerms} terms, then keep feeding
     * the accumulated result back with the next {@code maxTerms - 1} terms.
     * Left-associative, so concat order is preserved; union and intersection
     * are commutative and unaffected.
     */
    private CompletableFuture<Term> fold(
        Function<MultiTermsRequestDto, CompletableFuture<TermDto>> op,
        List<Term> terms,
        OperationOptions options,
        int maxTerms
    ) {
        CompletableFuture<Term> acc = naryCall(
            op,
            terms.subList(0, maxTerms),
            options,
            false
        );
        int index = maxTerms;
        while (index < terms.size()) {
            int end = Math.min(index + maxTerms - 1, terms.size());
            final List<Term> chunk = terms.subList(index, end);
            final boolean isFinal = end >= terms.size();
            acc = acc.thenCompose(accTerm -> {
                List<Term> batch = new ArrayList<>();
                batch.add(accTerm);
                batch.addAll(chunk);
                return naryCall(op, batch, options, isFinal);
            });
            index = end;
        }
        return acc;
    }

    // --- ANALYZE OPERATIONS ---

    /**
     * Computes how many unique strings the term matches asynchronously.
     *
     * @param term The term to analyze.
     * @return A CompletableFuture containing a Cardinality object representing either an exact Integer, a BigInteger, or Infinite cardinality.
     */
    public CompletableFuture<Cardinality> getCardinality(Term term) {
        return getCardinality(term, (OperationOptions) null);
    }

    /**
     * Computes how many unique strings the term matches asynchronously.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return A CompletableFuture containing a Cardinality object representing either an exact Integer, a BigInteger, or Infinite cardinality.
     */
    public CompletableFuture<Cardinality> getCardinality(
        Term term,
        OperationOptions options
    ) {
        if (term.getCachedCardinality() != null) {
            return CompletableFuture.completedFuture(
                term.getCachedCardinality()
            );
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(options));
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
        return getLength(term, (OperationOptions) null);
    }

    /**
     * Computes the minimum and maximum length of strings matched by the term asynchronously.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return A CompletableFuture containing a Length object with `min` and `max` integers. Limits are null if unbounded or undefined.
     */
    public CompletableFuture<Length> getLength(
        Term term,
        OperationOptions options
    ) {
        if (term.getCachedLength() != null) {
            return CompletableFuture.completedFuture(term.getCachedLength());
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(options));
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
        return isEmpty(term, (OperationOptions) null);
    }

    /**
     * Checks if the term matches no strings at all asynchronously.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return A CompletableFuture containing true if the language is completely empty, false otherwise.
     */
    public CompletableFuture<Boolean> isEmpty(
        Term term,
        OperationOptions options
    ) {
        if (term.getCachedEmpty() != null) {
            return CompletableFuture.completedFuture(term.getCachedEmpty());
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(options));
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
        return isEmptyString(term, (OperationOptions) null);
    }

    /**
     * Checks if the term matches only the empty string asynchronously.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return A CompletableFuture containing true if the term strictly matches the empty string ("") and nothing else.
     */
    public CompletableFuture<Boolean> isEmptyString(
        Term term,
        OperationOptions options
    ) {
        if (term.getCachedEmptyString() != null) {
            return CompletableFuture.completedFuture(
                term.getCachedEmptyString()
            );
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(options));
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
        return isTotal(term, (OperationOptions) null);
    }

    /**
     * Checks if the term matches all possible strings asynchronously.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return A CompletableFuture containing true if the term matches every possible string.
     */
    public CompletableFuture<Boolean> isTotal(
        Term term,
        OperationOptions options
    ) {
        if (term.getCachedTotal() != null) {
            return CompletableFuture.completedFuture(term.getCachedTotal());
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(options));
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
     * Checks if the term's automaton is deterministic asynchronously.
     * Only a deterministic FAIR guarantees consistent string ordering across paginated generateStrings() calls; call determinize() first if this is false.
     *
     * @param term The term to analyze.
     * @return A CompletableFuture containing true if the term's automaton is deterministic.
     */
    public CompletableFuture<Boolean> isDeterministic(Term term) {
        return isDeterministic(term, (OperationOptions) null);
    }

    /**
     * Checks if the term's automaton is deterministic asynchronously.
     * Only a deterministic FAIR guarantees consistent string ordering across paginated generateStrings() calls; call determinize() first if this is false.
     *
     * @param term    The term to analyze.
     * @param options Options for the operation.
     * @return A CompletableFuture containing true if the term's automaton is deterministic.
     */
    public CompletableFuture<Boolean> isDeterministic(
        Term term,
        OperationOptions options
    ) {
        if (!(term instanceof Term.FairTerm)) {
            return CompletableFuture.completedFuture(false);
        }
        Term.FairTerm fairTerm = (Term.FairTerm) term;
        Optional<Boolean> cached = fairTerm.getCachedDeterministic();
        if (cached.isPresent()) {
            return CompletableFuture.completedFuture(cached.get());
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(options));
        return executeWithRetry(() -> analyzeApi.deterministic(request)).thenApply(
            resp -> {
                boolean val = resp.getData().getValue();
                fairTerm.setCachedDeterministic(Optional.of(val));
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
        return getPattern(term, (OperationOptions) null);
    }

    /**
     * Returns a regular expression pattern that represents the term asynchronously.
     *
     * @param term    The term to extract the pattern from.
     * @param options Options for the operation.
     * @return A CompletableFuture containing a valid regular expression string representing the language.
     */
    public CompletableFuture<String> getPattern(
        Term term,
        OperationOptions options
    ) {
        if (term.getCachedPattern() != null) {
            return CompletableFuture.completedFuture(term.getCachedPattern());
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(options));
        return executeWithRetry(() ->
            analyzeApi.pattern(request)
        ).thenApply(resp -> {
            String val = resp.getData().getValue();
            term.setCachedPattern(val);
            return val;
        });
    }

    /**
     * Builds a Graphviz DOT representation of the term's automaton asynchronously.
     *
     * @param term The term to visualize.
     * @return A CompletableFuture containing the raw DOT syntax for Graphviz compilation.
     */
    public CompletableFuture<String> getDot(Term term) {
        return getDot(term, (OperationOptions) null);
    }

    /**
     * Builds a Graphviz DOT representation of the term's automaton asynchronously.
     *
     * @param term    The term to visualize.
     * @param options Options for the operation.
     * @return A CompletableFuture containing the raw DOT syntax for Graphviz compilation.
     */
    public CompletableFuture<String> getDot(
        Term term,
        OperationOptions options
    ) {
        if (term.getCachedDot() != null) {
            return CompletableFuture.completedFuture(term.getCachedDot());
        }
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(options));
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
        return equivalent(term1, term2, (OperationOptions) null);
    }

    /**
     * Checks if the two terms accept exactly the same language asynchronously.
     *
     * @param term1   The first term.
     * @param term2   The second term to compare against.
     * @param options Options for the operation.
     * @return A CompletableFuture containing true if they are entirely equivalent, false otherwise.
     */
    public CompletableFuture<Boolean> equivalent(
        Term term1,
        Term term2,
        OperationOptions options
    ) {
        TwoTermsRequestDto request = new TwoTermsRequestDto()
            .addTermsItem(term1.toDto())
            .addTermsItem(term2.toDto())
            .options(buildOptions(options));
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
        return subset(subset, superset, (OperationOptions) null);
    }

    /**
     * Checks if the first term's language is a subset of the second term's language asynchronously.
     *
     * @param subset   The term to test as the subset.
     * @param superset The term representing the entire set space.
     * @param options  Options for the operation.
     * @return A CompletableFuture containing true if every string matched by subset is also matched by superset.
     */
    public CompletableFuture<Boolean> subset(
        Term subset,
        Term superset,
        OperationOptions options
    ) {
        TwoTermsRequestDto request = new TwoTermsRequestDto()
            .addTermsItem(subset.toDto())
            .addTermsItem(superset.toDto())
            .options(buildOptions(options));
        return executeWithRetry(() -> analyzeApi.subset(request)).thenApply(
            resp -> resp.getData().getValue()
        );
    }

    // --- COMPUTE OPERATIONS ---

    /**
     * Concatenates the given terms sequentially asynchronously.
     *
     * @param terms Variadic terms to concatenate in order.
     * @return A CompletableFuture containing a newly computed concatenated term.
     */
    public CompletableFuture<Term> concat(Term... terms) {
        return concat(Arrays.asList(terms));
    }

    /**
     * Concatenates the given terms sequentially asynchronously.
     *
     * @param terms A dynamic list of terms to concatenate in order.
     * @return A CompletableFuture containing a newly computed concatenated term.
     */
    public CompletableFuture<Term> concat(List<Term> terms) {
        return concat(terms, (OperationOptions) null);
    }

    /**
     * Concatenates the given terms sequentially asynchronously.
     *
     * @param terms   A dynamic list of terms to concatenate in order.
     * @param options Options for the operation.
     * @return A CompletableFuture containing a newly computed concatenated term.
     */
    public CompletableFuture<Term> concat(
        List<Term> terms,
        OperationOptions options
    ) {
        return runNary(terms, options, request ->
            executeWithRetry(() -> computeApi.concat(request)).thenApply(
                Concat200ResponseDto::getData
            )
        );
    }

    /**
     * Computes the intersection of the given terms asynchronously.
     *
     * @param terms Variadic terms to intersect.
     * @return A CompletableFuture containing a term representing only strings matched by ALL provided terms.
     */
    public CompletableFuture<Term> intersection(Term... terms) {
        return intersection(Arrays.asList(terms));
    }

    /**
     * Computes the intersection of the given terms asynchronously.
     *
     * @param terms A dynamic list of terms to intersect.
     * @return A CompletableFuture containing a term representing only strings matched by ALL provided terms.
     */
    public CompletableFuture<Term> intersection(List<Term> terms) {
        return intersection(terms, (OperationOptions) null);
    }

    /**
     * Computes the intersection of the given terms asynchronously.
     *
     * @param terms   A dynamic list of terms to intersect.
     * @param options Options for the operation.
     * @return A CompletableFuture containing a term representing only strings matched by ALL provided terms.
     */
    public CompletableFuture<Term> intersection(
        List<Term> terms,
        OperationOptions options
    ) {
        return runNary(terms, options, request ->
            executeWithRetry(() -> computeApi.intersection(request)).thenApply(
                Concat200ResponseDto::getData
            )
        );
    }

    /**
     * Computes the union of the given terms asynchronously.
     *
     * @param terms Variadic terms to combine.
     * @return A CompletableFuture containing a term representing strings matched by ANY of the provided terms.
     */
    public CompletableFuture<Term> union(Term... terms) {
        return union(Arrays.asList(terms));
    }

    /**
     * Computes the union of the given terms asynchronously.
     *
     * @param terms A dynamic list of terms to combine.
     * @return A CompletableFuture containing a term representing strings matched by ANY of the provided terms.
     */
    public CompletableFuture<Term> union(List<Term> terms) {
        return union(terms, (OperationOptions) null);
    }

    /**
     * Computes the union of the given terms asynchronously.
     *
     * @param terms   A dynamic list of terms to combine.
     * @param options Options for the operation.
     * @return A CompletableFuture containing a term representing strings matched by ANY of the provided terms.
     */
    public CompletableFuture<Term> union(
        List<Term> terms,
        OperationOptions options
    ) {
        return runNary(terms, options, request ->
            executeWithRetry(() -> computeApi.union(request)).thenApply(
                Concat200ResponseDto::getData
            )
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
        return difference(base, excluded, (OperationOptions) null);
    }

    /**
     * Computes the difference between the two provided terms asynchronously.
     *
     * @param base     The base language term to subtract from.
     * @param excluded The term whose language should be removed from the base.
     * @param options  Options for the operation.
     * @return A CompletableFuture containing a computed difference term.
     */
    public CompletableFuture<Term> difference(
        Term base,
        Term excluded,
        OperationOptions options
    ) {
        TwoTermsRequestDto request = new TwoTermsRequestDto()
            .addTermsItem(base.toDto())
            .addTermsItem(excluded.toDto())
            .options(buildOptions(options));
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
        return complement(term, (OperationOptions) null);
    }

    /**
     * Computes the complement of the given term asynchronously.
     *
     * @param term    The term to complement.
     * @param options Options for the operation.
     * @return A CompletableFuture containing the complemented term.
     */
    public CompletableFuture<Term> complement(
        Term term,
        OperationOptions options
    ) {
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(options));
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
        return repeat(term, min, max, (OperationOptions) null);
    }

    /**
     * Repeats a term between a minimum and maximum number of times asynchronously.
     *
     * @param term    The term to repeat.
     * @param min     The inclusive lower bound of repetitions.
     * @param max     The inclusive upper bound. If null, repetitions are unbounded.
     * @param options  Options for the operation.
     * @return A CompletableFuture containing a computed repeated term.
     */
    public CompletableFuture<Term> repeat(
        Term term,
        int min,
        Integer max,
        OperationOptions options
    ) {
        RepeatRequestDto request = new RepeatRequestDto()
            .term(term.toDto())
            .min(min)
            .max(max)
            .options(buildOptions(options));
        return executeWithRetry(() -> computeApi.repeat(request)).thenApply(
            resp -> Term.fromDto(resp.getData())
        );
    }

    /**
     * Computes a deterministic FAIR automaton from the given term asynchronously.
     * A deterministic FAIR guarantees consistent string ordering across paginated
     * generateStrings() calls. Use this when isDeterministic() is false
     * before calling generateStrings() with an offset.
     *
     * @param term The term to determinize.
     * @return A CompletableFuture containing a deterministic FAIR.
     */
    public CompletableFuture<Term> determinize(Term term) {
        return determinize(term, (OperationOptions) null);
    }

    /**
     * Computes a deterministic FAIR automaton from the given term asynchronously.
     * A deterministic FAIR guarantees consistent string ordering across paginated
     * generateStrings() calls. Use this when isDeterministic() is false
     * before calling generateStrings() with an offset.
     *
     * @param term    The term to determinize.
     * @param options Options for the operation.
     * @return A CompletableFuture containing a deterministic FAIR.
     */
    public CompletableFuture<Term> determinize(
        Term term,
        OperationOptions options
    ) {
        TermRequestDto request = new TermRequestDto()
            .term(term.toDto())
            .options(buildOptions(options));
        return executeWithRetry(() -> computeApi.determinize(request)).thenApply(
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
        return generateStrings(term, limit, offset, (OperationOptions) null);
    }

    /**
     * Generates up to {@code limit} distinct strings matched by the term, skipping the first {@code offset} strings asynchronously.
     *
     * @param term    The term to sample generated strings from.
     * @param limit   The maximum number of unique strings to return.
     * @param offset  Number of matched strings to skip before starting to collect the results. Used for pagination.
     * @param options Options for the operation. Pass a {@link GenerateStringsOptions}
     *                to control ordering, seed, length bounds and charset.
     * @return A CompletableFuture containing a list of strings that match the term.
     */
    public CompletableFuture<List<String>> generateStrings(
        Term term,
        int limit,
        int offset,
        OperationOptions options
    ) {
        GenerateStringsRequestDto request = new GenerateStringsRequestDto()
            .term(term.toDto())
            .limit(limit)
            .offset(offset)
            .options(buildOptions(options));

        if (options instanceof GenerateStringsOptions) {
            GenerateStringsOptions generateOptions =
                (GenerateStringsOptions) options;
            generateOptions
                .getPathOrder()
                .ifPresent(value -> request.pathOrder(value.toDto()));
            generateOptions
                .getCharacterOrder()
                .ifPresent(value -> request.characterOrder(value.toDto()));
            generateOptions.getSeed().ifPresent(request::seed);
            generateOptions.getMinLength().ifPresent(request::minLength);
            generateOptions.getMaxLength().ifPresent(request::maxLength);
            generateOptions.getCharset().ifPresent(request::charset);
        }

        return executeWithRetry(() -> generateApi.strings(request)).thenApply(
            resp -> resp.getData().getStrings().getValue()
        );
    }
}
