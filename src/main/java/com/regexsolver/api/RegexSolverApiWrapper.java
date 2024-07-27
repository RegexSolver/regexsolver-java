package com.regexsolver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regexsolver.api.Request.GenerateStringsRequest;
import com.regexsolver.api.Request.MultiTermsRequest;
import com.regexsolver.api.Response.BooleanResponse;
import com.regexsolver.api.Response.StringsResponse;
import com.regexsolver.api.dto.Details;
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

final class RegexSolverApiWrapper {
    private static final RegexSolverApiWrapper INSTANCE = new RegexSolverApiWrapper();

    private final static String DEFAULT_BASE_URL = "https://api.regexsolver.com/";

    private final static String USER_AGENT = "RegexSolver Java / 1.0.1";

    private RegexApi api;

    public static RegexSolverApiWrapper getInstance() {
        return INSTANCE;
    }

    private RegexSolverApiWrapper() {
        initializeInternal(null, DEFAULT_BASE_URL);
    }

    static void initialize(String token) {
        getInstance().initializeInternal(token, DEFAULT_BASE_URL);
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

    public Term computeIntersection(MultiTermsRequest multiTermsRequest) throws ApiError, IOException {
        Response<Term> response = api.computeIntersection(multiTermsRequest).execute();
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

    public Term computeSubtraction(MultiTermsRequest multiTermsRequest) throws ApiError, IOException {
        Response<Term> response = api.computeSubtraction(multiTermsRequest).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw getApiError(response);
        }
    }

    public Details getDetails(Term term) throws ApiError, IOException {
        Response<Details> response = api.getDetails(term).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw getApiError(response);
        }
    }

    public boolean equivalence(MultiTermsRequest multiTermsRequest) throws ApiError, IOException {
        Response<BooleanResponse> response = api.equivalence(multiTermsRequest).execute();
        if (response.isSuccessful()) {
            return response.body().value();
        } else {
            throw getApiError(response);
        }
    }

    public boolean subset(MultiTermsRequest multiTermsRequest) throws ApiError, IOException {
        Response<BooleanResponse> response = api.subset(multiTermsRequest).execute();
        if (response.isSuccessful()) {
            return response.body().value();
        } else {
            throw getApiError(response);
        }
    }

    public List<String> generateStrings(Term term, int count) throws ApiError, IOException {
        GenerateStringsRequest generateStringsRequest = new GenerateStringsRequest(term, count);
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
        @POST("api/compute/intersection")
        Call<Term> computeIntersection(@Body MultiTermsRequest multiTermsRequest);

        @POST("api/compute/union")
        Call<Term> computeUnion(@Body MultiTermsRequest multiTermsRequest);

        @POST("api/compute/subtraction")
        Call<Term> computeSubtraction(@Body MultiTermsRequest multiTermsRequest);

        @POST("api/analyze/details")
        Call<Details> getDetails(@Body Term term);

        @POST("api/analyze/equivalence")
        Call<BooleanResponse> equivalence(@Body MultiTermsRequest multiTermsRequest);

        @POST("api/analyze/subset")
        Call<BooleanResponse> subset(@Body MultiTermsRequest multiTermsRequest);

        @POST("api/generate/strings")
        Call<StringsResponse> generateStrings(@Body GenerateStringsRequest request);
    }
}
