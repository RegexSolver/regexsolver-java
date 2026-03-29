package com.regexsolver.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegexSolverClientTest {

    @Mock
    private AsyncRegexSolverClient asyncClient;

    private RegexSolverClient client;

    @BeforeEach
    void setUp() throws Exception {
        // Build the synchronous client
        client = RegexSolverClient.builder().apiToken("test-token").build();

        // Use reflection to inject the mocked Async client into the private field
        Field field = RegexSolverClient.class.getDeclaredField("asyncClient");
        field.setAccessible(true);
        field.set(client, asyncClient);
    }

    @Test
    void testSyncClientGetCardinality() {
        Term term = Term.regex("abc");
        Cardinality.Integer mockResult = new Cardinality.Integer(42L);

        when(asyncClient.getCardinality(any())).thenReturn(
            CompletableFuture.completedFuture(mockResult)
        );

        Cardinality result = client.getCardinality(term);

        assertThat(result).isInstanceOf(Cardinality.Integer.class);
        assertThat(((Cardinality.Integer) result).getValue()).isEqualTo(42L);

        // Verify the async client was called
        verify(asyncClient).getCardinality(term);
    }

    @Test
    void testSyncClientIsEmpty() {
        Term term = Term.regex("abc");

        when(asyncClient.isEmpty(any())).thenReturn(
            CompletableFuture.completedFuture(false)
        );

        boolean result = client.isEmpty(term);

        assertThat(result).isFalse();
        verify(asyncClient).isEmpty(term);
    }

    @Test
    void testSyncClientUnion() {
        Term term1 = Term.regex("a");
        Term term2 = Term.regex("b");
        Term mockResultTerm = Term.regex("a|b");
        List<Term> termList = List.of(term1, term2);

        // We mock the format and timeout overloaded method since the base
        // concat/union/intersection methods in AsyncClient pass 'null' down.
        when(asyncClient.union(any(), isNull(), isNull())).thenReturn(
            CompletableFuture.completedFuture(mockResultTerm)
        );

        // Let's assume the sync client passes down to the async client's 3-arg method
        Term result = client.union(termList);

        assertThat(result.getPattern()).contains("a|b");
        verify(asyncClient).union(termList);
    }

    @Test
    void testSyncClientComplement() {
        Term term = Term.regex(".*a.*");
        Term mockResultTerm = Term.regex("[^a].*");

        when(asyncClient.complement(any())).thenReturn(
            CompletableFuture.completedFuture(mockResultTerm)
        );

        Term result = client.complement(term);

        assertThat(result.getPattern()).contains("[^a].*");
        verify(asyncClient).complement(term);
    }

    @Test
    void testSyncClientGetLength() {
        Term term = Term.regex("(abc)?d");
        Length mockLength = new Length(1, 4);

        when(asyncClient.getLength(any())).thenReturn(
            CompletableFuture.completedFuture(mockLength)
        );

        Length result = client.getLength(term);

        assertThat(result.getMin()).isEqualTo(1);
        assertThat(result.getMax()).isEqualTo(4);
        verify(asyncClient).getLength(term);
    }

    @Test
    void testSyncClientIntersection() {
        Term term1 = Term.regex("a");
        Term term2 = Term.regex("ab");
        Term mockResultTerm = Term.regex("a");
        List<Term> termList = List.of(term1, term2);

        when(asyncClient.intersection(any(), isNull(), isNull())).thenReturn(
            CompletableFuture.completedFuture(mockResultTerm)
        );

        Term result = client.intersection(termList);

        assertThat(result.getPattern()).contains("a");
        verify(asyncClient).intersection(termList);
    }

    @Test
    void testSyncClientGenerateStrings() {
        Term term = Term.regex("a*");
        List<String> mockStrings = List.of("", "a", "aa");

        when(asyncClient.generateStrings(any(), eq(3), eq(0))).thenReturn(
            CompletableFuture.completedFuture(mockStrings)
        );

        List<String> result = client.generateStrings(term, 3, 0);

        assertThat(result).containsExactly("", "a", "aa");
        verify(asyncClient).generateStrings(term, 3, 0);
    }
}
