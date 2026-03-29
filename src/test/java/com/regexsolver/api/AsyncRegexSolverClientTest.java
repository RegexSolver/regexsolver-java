package com.regexsolver.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.regexsolver.api.exceptions.*;
import com.regexsolver.api.generated.ApiException;
import com.regexsolver.api.generated.api.AnalyzeApi;
import com.regexsolver.api.generated.api.ComputeApi;
import com.regexsolver.api.generated.api.GenerateApi;
import com.regexsolver.api.generated.model.*;
import java.lang.reflect.Field;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AsyncRegexSolverClientTest {

    @Mock
    private AnalyzeApi analyzeApi;

    @Mock
    private ComputeApi computeApi;

    @Mock
    private GenerateApi generateApi;

    private AsyncRegexSolverClient client;

    @BeforeEach
    void setUp() throws Exception {
        // Build the real client
        client = AsyncRegexSolverClient.builder()
            .apiToken("test-token")
            .build();

        // Use reflection to inject the mocks into the final class fields
        injectMock(client, "analyzeApi", analyzeApi);
        injectMock(client, "computeApi", computeApi);
        injectMock(client, "generateApi", generateApi);
    }

    private void injectMock(Object target, String fieldName, Object mock)
        throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testGetCardinalityInteger() {
        Term term = Term.regex("abc");

        Cardinality200ResponseDto responseDto = new Cardinality200ResponseDto();
        CardinalityDto data = new CardinalityDto(
            new CardinalityIntegerDto().value(42L)
        );
        responseDto.setData(data);

        when(analyzeApi.cardinality(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        Cardinality result = client.getCardinality(term).join();

        assertThat(result).isInstanceOf(Cardinality.Integer.class);
        assertThat(((Cardinality.Integer) result).getValue()).isEqualTo(42L);
        assertThat(term.getCachedCardinality()).isEqualTo(result);
    }

    @Test
    void testGetCardinalityInfinite() {
        Term term = Term.regex(".*");

        Cardinality200ResponseDto responseDto = new Cardinality200ResponseDto();
        CardinalityDto data = new CardinalityDto(new CardinalityInfiniteDto());
        responseDto.setData(data);

        when(analyzeApi.cardinality(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        Cardinality result = client.getCardinality(term).join();

        assertThat(result).isInstanceOf(Cardinality.Infinite.class);
    }

    @Test
    void testGetLength() {
        Term term = Term.regex("abc");

        Length200ResponseDto responseDto = new Length200ResponseDto();
        LengthDto data = new LengthDto();
        data.setMin(3);
        data.setMax(3);
        responseDto.setData(data);

        when(analyzeApi.length(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        Length result = client.getLength(term).join();

        assertThat(result.getMin()).isEqualTo(3);
        assertThat(result.getMax()).isEqualTo(3);
    }

    @Test
    void testIsEmpty() {
        Term term = Term.regex("[]");

        Empty200ResponseDto responseDto = new Empty200ResponseDto();
        BooleanDto data = new BooleanDto();
        data.setValue(true);
        responseDto.setData(data);

        when(analyzeApi.empty(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        Boolean result = client.isEmpty(term).join();

        assertThat(result).isTrue();
        assertThat(term.getCachedEmpty()).isTrue();
    }

    @Test
    void testComputeUnion() {
        Term term1 = Term.regex("a");
        Term term2 = Term.regex("b");

        Concat200ResponseDto responseDto = new Concat200ResponseDto();
        TermDto data = new TermDto(new TermRegexDto().value("a|b"));
        responseDto.setData(data);

        when(computeApi.union(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        Term result = client.union(List.of(term1, term2)).join();

        assertThat(result).isNotNull();
        assertThat(result.getPattern()).contains("a|b");
    }

    @Test
    void testErrorHandling400_BadRequest() {
        Term term = Term.regex("invalid[");

        String body = "{\"error\": \"Invalid regex\"}";
        ApiException apiException = new ApiException(
            400,
            "Bad Request",
            null,
            body
        );

        when(analyzeApi.empty(any())).thenReturn(
            CompletableFuture.failedFuture(apiException)
        );

        assertThatThrownBy(() -> client.isEmpty(term).join())
            .hasCauseInstanceOf(BadRequestException.class)
            .hasMessageContaining("Invalid regex");
    }

    @Test
    void testErrorHandling_InvalidJson() {
        String body =
            "{\"success\": false, \"error\": \"Invalid JSON\", \"errorCode\": \"InvalidJson\"}";
        ApiException apiException = new ApiException(
            400,
            "Bad Request",
            null,
            body
        );

        when(analyzeApi.empty(any())).thenReturn(
            CompletableFuture.failedFuture(apiException)
        );

        assertThatThrownBy(() ->
            client.isEmpty(Term.regex("abc")).join()
        ).hasCauseInstanceOf(InvalidJsonException.class);
    }

    @Test
    void testErrorHandling_TooManyTerms() {
        String body =
            "{\"success\": false, \"error\": \"Too many terms\", \"errorCode\": \"TooManyTerms\"}";
        ApiException apiException = new ApiException(
            400,
            "Bad Request",
            null,
            body
        );

        when(computeApi.union(any())).thenReturn(
            CompletableFuture.failedFuture(apiException)
        );

        assertThatThrownBy(() ->
            client.union(List.of(Term.regex("a"), Term.regex("b"))).join()
        ).hasCauseInstanceOf(TooManyTermsException.class);
    }

    @Test
    void testErrorHandling_TimeoutTooLarge() {
        String body =
            "{\"success\": false, \"error\": \"Timeout too large\", \"errorCode\": \"TimeoutTooLarge\"}";
        ApiException apiException = new ApiException(
            400,
            "Bad Request",
            null,
            body
        );

        when(analyzeApi.empty(any())).thenReturn(
            CompletableFuture.failedFuture(apiException)
        );

        assertThatThrownBy(() ->
            client.isEmpty(Term.regex("abc")).join()
        ).hasCauseInstanceOf(TimeoutTooLargeException.class);
    }

    @Test
    void testErrorHandling_MissingOrMalformedToken() {
        String body =
            "{\"success\": false, \"error\": \"Missing token\", \"errorCode\": \"MissingOrMalformedToken\"}";
        ApiException apiException = new ApiException(
            401,
            "Unauthorized",
            null,
            body
        );

        when(analyzeApi.empty(any())).thenReturn(
            CompletableFuture.failedFuture(apiException)
        );

        assertThatThrownBy(() ->
            client.isEmpty(Term.regex("abc")).join()
        ).hasCauseInstanceOf(MissingOrMalformedTokenException.class);
    }

    @Test
    void testErrorHandling_QuotaExceeded() {
        String body =
            "{\"success\": false, \"error\": \"Quota exceeded\", \"errorCode\": \"QuotaExceeded\"}";
        ApiException apiException = new ApiException(
            403,
            "Forbidden",
            null,
            body
        );

        when(analyzeApi.empty(any())).thenReturn(
            CompletableFuture.failedFuture(apiException)
        );

        assertThatThrownBy(() ->
            client.isEmpty(Term.regex("abc")).join()
        ).hasCauseInstanceOf(QuotaExceededException.class);
    }

    @Test
    void testErrorHandling_500() {
        ApiException apiException = new ApiException(
            500,
            "Internal Server Error",
            null,
            null
        );

        when(analyzeApi.empty(any())).thenReturn(
            CompletableFuture.failedFuture(apiException)
        );

        assertThatThrownBy(() ->
            client.isEmpty(Term.regex("abc")).join()
        ).hasCauseInstanceOf(InternalServerException.class);
    }

    @Test
    void testErrorHandling_OtherApiError() {
        ApiException apiException = new ApiException(
            418,
            "I'm a teapot",
            null,
            null
        );

        when(analyzeApi.empty(any())).thenReturn(
            CompletableFuture.failedFuture(apiException)
        );

        assertThatThrownBy(() -> client.isEmpty(Term.regex("abc")).join())
            .hasCauseInstanceOf(
                com.regexsolver.api.exceptions.ApiException.class
            )
            .satisfies(e ->
                assertThat(
                    (
                        (com.regexsolver.api.exceptions.ApiException) e.getCause()
                    ).getStatusCode()
                ).isEqualTo(418)
            );
    }

    @Test
    void testRetryOn429() {
        Term term = Term.regex("abc");

        HttpHeaders mockHeaders = mock(HttpHeaders.class);
        when(mockHeaders.firstValue("Retry-After")).thenReturn(
            Optional.of("0.1")
        );
        ApiException error429 = new ApiException(
            429,
            "Too Many Requests",
            mockHeaders,
            null
        );

        Empty200ResponseDto successResponse = new Empty200ResponseDto();
        BooleanDto data = new BooleanDto();
        data.setValue(true);
        successResponse.setData(data);

        // First call fails with 429, second call succeeds
        when(analyzeApi.empty(any()))
            .thenReturn(CompletableFuture.failedFuture(error429))
            .thenReturn(CompletableFuture.completedFuture(successResponse));

        Boolean result = client.isEmpty(term).join();

        assertThat(result).isTrue();
        // Verify it was called exactly twice
        verify(analyzeApi, times(2)).empty(any());
    }

    @Test
    void testEquivalent() {
        Term term1 = Term.regex("a");
        Term term2 = Term.regex("a");
        Empty200ResponseDto responseDto = new Empty200ResponseDto();
        BooleanDto data = new BooleanDto();
        data.setValue(true);
        responseDto.setData(data);

        when(analyzeApi.equivalent(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        assertThat(client.equivalent(term1, term2).join()).isTrue();
    }

    @Test
    void testSubset() {
        Term term1 = Term.regex("a");
        Term term2 = Term.regex("a|b");
        Empty200ResponseDto responseDto = new Empty200ResponseDto();
        BooleanDto data = new BooleanDto();
        data.setValue(true);
        responseDto.setData(data);

        when(analyzeApi.subset(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        assertThat(client.subset(term1, term2).join()).isTrue();
    }

    @Test
    void testIsEmptyString() {
        Term term = Term.regex("");
        Empty200ResponseDto responseDto = new Empty200ResponseDto();
        BooleanDto data = new BooleanDto();
        data.setValue(true);
        responseDto.setData(data);

        when(analyzeApi.emptyString(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        assertThat(client.isEmptyString(term).join()).isTrue();
    }

    @Test
    void testIsTotal() {
        Term term = Term.regex(".*");
        Empty200ResponseDto responseDto = new Empty200ResponseDto();
        BooleanDto data = new BooleanDto();
        data.setValue(true);
        responseDto.setData(data);

        when(analyzeApi.total(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        assertThat(client.isTotal(term).join()).isTrue();
    }

    @Test
    void testGetPattern() {
        Term term = Term.regex("a");
        Dot200ResponseDto responseDto = new Dot200ResponseDto();
        StringDto data = new StringDto();
        data.setValue("a");
        responseDto.setData(data);

        when(analyzeApi.pattern(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        assertThat(client.getPattern(term).join()).isEqualTo("a");
    }

    @Test
    void testGetDot() {
        Term term = Term.regex("a");
        Dot200ResponseDto responseDto = new Dot200ResponseDto();
        StringDto data = new StringDto();
        data.setValue("digraph {...}");
        responseDto.setData(data);

        when(analyzeApi.dot(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        assertThat(client.getDot(term).join()).isEqualTo("digraph {...}");
    }

    @Test
    void testConcat() {
        Term term1 = Term.regex("a");
        Term term2 = Term.regex("b");
        Concat200ResponseDto responseDto = new Concat200ResponseDto();
        TermDto data = new TermDto(new TermRegexDto().value("ab"));
        responseDto.setData(data);

        when(computeApi.concat(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        Term result = client.concat(List.of(term1, term2)).join();
        assertThat(result.getPattern()).contains("ab");
    }

    @Test
    void testIntersection() {
        Term term1 = Term.regex("a.");
        Term term2 = Term.regex(".b");
        Concat200ResponseDto responseDto = new Concat200ResponseDto();
        TermDto data = new TermDto(new TermRegexDto().value("ab"));
        responseDto.setData(data);

        when(computeApi.intersection(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        Term result = client.intersection(List.of(term1, term2)).join();
        assertThat(result.getPattern()).contains("ab");
    }

    @Test
    void testDifference() {
        Term term1 = Term.regex("a|b");
        Term term2 = Term.regex("b");
        Concat200ResponseDto responseDto = new Concat200ResponseDto();
        TermDto data = new TermDto(new TermRegexDto().value("a"));
        responseDto.setData(data);

        when(computeApi.difference(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        Term result = client.difference(term1, term2).join();
        assertThat(result.getPattern()).contains("a");
    }

    @Test
    void testRepeat() {
        Term term = Term.regex("a");
        Concat200ResponseDto responseDto = new Concat200ResponseDto();
        TermDto data = new TermDto(new TermRegexDto().value("a{2,3}"));
        responseDto.setData(data);

        when(computeApi.repeat(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        Term result = client.repeat(term, 2, 3).join();
        assertThat(result.getPattern()).contains("a{2,3}");
    }

    @Test
    void testComplement() {
        Term term = Term.regex(".*a.*");
        Concat200ResponseDto responseDto = new Concat200ResponseDto();
        TermDto data = new TermDto(new TermRegexDto().value("[^a].*"));
        responseDto.setData(data);

        when(computeApi.complement(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        Term result = client.complement(term).join();
        assertThat(result.getPattern()).contains("[^a].*");
    }

    @Test
    void testGenerateStrings() {
        Term term = Term.regex("a*");
        Strings200ResponseDto responseDto = new Strings200ResponseDto();
        StringsDto stringsDto = new StringsDto();
        stringsDto.setValue(List.of("", "a", "aa"));
        GenerateStringsResponseDto generateStrings =
            new GenerateStringsResponseDto();
        generateStrings.setStrings(stringsDto);
        responseDto.setData(generateStrings);

        when(generateApi.strings(any())).thenReturn(
            CompletableFuture.completedFuture(responseDto)
        );

        List<String> result = client.generateStrings(term, 3, 0).join();
        assertThat(result).containsExactly("", "a", "aa");
    }
}
