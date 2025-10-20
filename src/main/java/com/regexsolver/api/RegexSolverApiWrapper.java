package com.regexsolver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regexsolver.api.Request.GenerateStringsRequest;
import com.regexsolver.api.Request.MultiTermsRequest;
import com.regexsolver.api.Request.RepeatRequest;
import com.regexsolver.api.Response.BooleanResponse;
import com.regexsolver.api.Response.StringResponse;
import com.regexsolver.api.Response.StringsResponse;
import com.regexsolver.api.dto.Cardinality;
import com.regexsolver.api.dto.Details;
import com.regexsolver.api.dto.Length;
import com.regexsolver.api.exception.ApiError;
import com.regexsolver.api.exception.MissingAPITokenException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class RegexSolverApiWrapper {
    private static final RegexSolverApiWrapper INSTANCE = new RegexSolverApiWrapper();

    private final static String DEFAULT_BASE_URL = "https://api.regexsolver.com/";

    private final static String USER_AGENT = "RegexSolver Java / 1.1.0";

    private RegexApi api;

    public static RegexSolverApiWrapper getInstance() {
        return INSTANCE;
    }

    private RegexSolverApiWrapper() {
        initializeInternal(null, DEFAULT_BASE_URL);
    }

    private static String getConfiguredBaseUrl() {
        return Optional.ofNullable(System.getenv("REGEXSOLVER_BASE_URL")).orElse(DEFAULT_BASE_URL);
    }

    static void initialize() {
        getInstance().initializeInternal(System.getenv("REGEXSOLVER_API_TOKEN"), getConfiguredBaseUrl());
    }

    static void initialize(String token) {
        getInstance().initializeInternal(token, getConfiguredBaseUrl());
    }

    static void initialize(String token, String baseUrl) {
        getInstance().initializeInternal(token, baseUrl);
    }

    private void initializeInternal(String token, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .client(new OkHttpClient.Builder().addInterceptor(chain -> {
                    if (token == null) {
                        throw new MissingAPITokenException();
                    }
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("User-Agent", USER_AGENT)
                            .addHeader("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(newRequest);
                }).build())
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        api = retrofit.create(RegexApi.class);
    }

    // Analyze

    public Cardinality analyzeCardinality(Term term) throws ApiError, IOException {
        Response<Cardinality> response = api.analyzeCardinality(term).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw getApiError(response);
        }
    }

    public Details analyzeDetails(Term term) throws ApiError, IOException {
        Response<Details> response = api.analyzeDetails(term).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw getApiError(response);
        }
    }

    public String analyzeDot(Term term) throws ApiError, IOException {
        Response<StringResponse> response = api.analyzeDot(term).execute();
        if (response.isSuccessful()) {
            return response.body().value();
        } else {
            throw getApiError(response);
        }
    }

    public boolean analyzeEquivalent(MultiTermsRequest multiTermsRequest) throws ApiError, IOException {
        Response<BooleanResponse> response = api.analyzeEquivalent(multiTermsRequest).execute();
        if (response.isSuccessful()) {
            return response.body().value();
        } else {
            throw getApiError(response);
        }
    }

    public boolean analyzeEmpty(Term term) throws ApiError, IOException {
        Response<BooleanResponse> response = api.analyzeEmpty(term).execute();
        if (response.isSuccessful()) {
            return response.body().value();
        } else {
            throw getApiError(response);
        }
    }

    public boolean analyzeEmptyString(Term term) throws ApiError, IOException {
        Response<BooleanResponse> response = api.analyzeEmptyString(term).execute();
        if (response.isSuccessful()) {
            return response.body().value();
        } else {
            throw getApiError(response);
        }
    }

    public Length analyzeLength(Term term) throws ApiError, IOException {
        Response<Length> response = api.analyzeLength(term).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw getApiError(response);
        }
    }

    public String analyzePattern(Term term) throws ApiError, IOException {
        Response<StringResponse> response = api.analyzePattern(term).execute();
        if (response.isSuccessful()) {
            return response.body().value();
        } else {
            throw getApiError(response);
        }
    }

    public boolean analyzeSubset(MultiTermsRequest multiTermsRequest) throws ApiError, IOException {
        Response<BooleanResponse> response = api.analyzeSubset(multiTermsRequest).execute();
        if (response.isSuccessful()) {
            return response.body().value();
        } else {
            throw getApiError(response);
        }
    }

    public boolean analyzeTotal(Term term) throws ApiError, IOException {
        Response<BooleanResponse> response = api.analyzeTotal(term).execute();
        if (response.isSuccessful()) {
            return response.body().value();
        } else {
            throw getApiError(response);
        }
    }

    // Compute

    public Term computeConcat(MultiTermsRequest multiTermsRequest) throws ApiError, IOException {
        Response<Term> response = api.computeConcat(multiTermsRequest).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw getApiError(response);
        }
    }

    public Term computeDifference(MultiTermsRequest multiTermsRequest) throws ApiError, IOException {
        Response<Term> response = api.computeDifference(multiTermsRequest).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw getApiError(response);
        }
    }

    public Term computeIntersection(MultiTermsRequest multiTermsRequest) throws ApiError, IOException {
        Response<Term> response = api.computeIntersection(multiTermsRequest).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw getApiError(response);
        }
    }

    public Term computeRepeat(RepeatRequest repeatRequest) throws ApiError, IOException {
        Response<Term> response = api.computeRepeat(repeatRequest).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw getApiError(response);
        }
    }

    public Term computeUnion(MultiTermsRequest multiTermsRequest) throws ApiError, IOException {
        Response<Term> response = api.computeUnion(multiTermsRequest).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw getApiError(response);
        }
    }

    // Generate

    public List<String> generateStrings(GenerateStringsRequest generateStringsRequest) throws ApiError, IOException {
        Response<StringsResponse> response = api.generateStrings(generateStringsRequest).execute();
        if (response.isSuccessful()) {
            return response.body().value();
        } else {
            throw getApiError(response);
        }
    }

    private static <T> ApiError getApiError(Response<T> response) throws IOException {
        assert !response.isSuccessful();
        try (ResponseBody errorBody = response.errorBody()) {
            String json = Objects.requireNonNull(errorBody).string();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, ApiError.class);
        }
    }

    private interface RegexApi {
        // analyze
        @POST("api/analyze/cardinality")
        Call<Cardinality> analyzeCardinality(@Body Term term);

        @POST("api/analyze/details")
        Call<Details> analyzeDetails(@Body Term term);

        @POST("api/analyze/dot")
        Call<StringResponse> analyzeDot(@Body Term term);

        @POST("api/analyze/equivalent")
        Call<BooleanResponse> analyzeEquivalent(@Body MultiTermsRequest multiTermsRequest);

        @POST("api/analyze/empty")
        Call<BooleanResponse> analyzeEmpty(@Body Term term);

        @POST("api/analyze/empty_string")
        Call<BooleanResponse> analyzeEmptyString(@Body Term term);

        @POST("api/analyze/length")
        Call<Length> analyzeLength(@Body Term term);

        @POST("api/analyze/pattern")
        Call<StringResponse> analyzePattern(@Body Term term);

        @POST("api/analyze/subset")
        Call<BooleanResponse> analyzeSubset(@Body MultiTermsRequest multiTermsRequest);

        @POST("api/analyze/total")
        Call<BooleanResponse> analyzeTotal(@Body Term term);

        // compute
        @POST("api/compute/concat")
        Call<Term> computeConcat(@Body MultiTermsRequest multiTermsRequest);

        @POST("api/compute/difference")
        Call<Term> computeDifference(@Body MultiTermsRequest multiTermsRequest);

        @POST("api/compute/intersection")
        Call<Term> computeIntersection(@Body MultiTermsRequest multiTermsRequest);

        @POST("api/compute/repeat")
        Call<Term> computeRepeat(@Body RepeatRequest repeatRequest);

        @POST("api/compute/union")
        Call<Term> computeUnion(@Body MultiTermsRequest multiTermsRequest);

        // generate
        @POST("api/generate/strings")
        Call<StringsResponse> generateStrings(@Body GenerateStringsRequest request);
    }
}
