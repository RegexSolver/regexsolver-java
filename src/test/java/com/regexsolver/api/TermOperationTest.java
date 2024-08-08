package com.regexsolver.api;

import com.regexsolver.api.dto.Cardinality;
import com.regexsolver.api.dto.Details;
import com.regexsolver.api.dto.Length;
import com.regexsolver.api.exception.ApiError;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class TermOperationTest {
    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        RegexSolver.initialize("TOKEN", server.url("/").toString());
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void test_getDetails() throws IOException, ApiError, InterruptedException {
        int requestCount = server.getRequestCount();

        MockResponse response = TestUtils.generateMockResponse(TestUtils.getResourceFileContent("response_getDetails.json"));
        server.enqueue(response);

        Term.Regex regex = Term.regex("(abc|de)");

        Details details = regex.getDetails();

        Cardinality cardinality = details.getCardinality();
        assertTrue(cardinality instanceof Cardinality.Integer);
        assertEquals(2, ((Cardinality.Integer) cardinality).getCount());

        Length length = details.getLength();
        assertEquals(2L, length.getMinimum().getAsLong());
        assertEquals(3L, length.getMaximum().getAsLong());

        assertFalse(details.isEmpty());
        assertFalse(details.isTotal());

        RecordedRequest request = server.takeRequest();
        assertEquals("/api/analyze/details", request.getPath());
        assertEquals(regex, TestUtils.readBuffer(request.getBody(), Term.class));

        Details detailsInCache = regex.getDetails();
        assertEquals(details, detailsInCache);

        assertEquals(1, server.getRequestCount() - requestCount);
    }

    @Test
    public void test_generateStrings() throws IOException, ApiError, InterruptedException {
        MockResponse response = TestUtils.generateMockResponse(TestUtils.getResourceFileContent("response_generateStrings.json"));
        server.enqueue(response);

        Term.Regex regex = Term.regex("(abc|de){2}");

        List<String> strings = regex.generateStrings(10);
        assertEquals(4, strings.size());

        RecordedRequest request = server.takeRequest();
        assertEquals("/api/generate/strings", request.getPath());

        Request.GenerateStringsRequest generateStringsRequest = TestUtils.readBuffer(request.getBody(), Request.GenerateStringsRequest.class);
        assertEquals(10, generateStringsRequest.getCount());
        assertEquals(regex, generateStringsRequest.getTerm());
    }

    @Test
    public void test_intersection() throws IOException, ApiError, InterruptedException {
        MockResponse response = TestUtils.generateMockResponse(TestUtils.getResourceFileContent("response_intersection.json"));
        server.enqueue(response);

        Term.Regex term1 = Term.regex("(abc|de){2}");
        Term.Regex term2 = Term.regex("de.*");
        Term.Regex term3 = Term.regex(".*abc");

        Term result = term1.intersection(term2, term3);
        assertTrue(result instanceof Term.Regex);
        assertEquals("deabc", ((Term.Regex) result).getPattern());

        RecordedRequest request = server.takeRequest();
        assertEquals("/api/compute/intersection", request.getPath());

        Request.MultiTermsRequest multiTermsRequest = TestUtils.readBuffer(request.getBody(), Request.MultiTermsRequest.class);
        List<Term> terms = multiTermsRequest.getTerms();
        assertEquals(term1, terms.get(0));
        assertEquals(term2, terms.get(1));
        assertEquals(term3, terms.get(2));
    }

    @Test
    public void test_union() throws IOException, ApiError, InterruptedException {
        MockResponse response = TestUtils.generateMockResponse(TestUtils.getResourceFileContent("response_union.json"));
        server.enqueue(response);

        Term.Regex term1 = Term.regex("abc");
        Term.Regex term2 = Term.regex("de");
        Term.Regex term3 = Term.regex("fghi");

        Term result = term1.union(term2, term3);
        assertTrue(result instanceof Term.Regex);
        assertEquals("(abc|de|fghi)", ((Term.Regex) result).getPattern());

        RecordedRequest request = server.takeRequest();
        assertEquals("/api/compute/union", request.getPath());

        Request.MultiTermsRequest multiTermsRequest = TestUtils.readBuffer(request.getBody(), Request.MultiTermsRequest.class);
        List<Term> terms = multiTermsRequest.getTerms();
        assertEquals(term1, terms.get(0));
        assertEquals(term2, terms.get(1));
        assertEquals(term3, terms.get(2));
    }

    @Test
    public void test_subtraction() throws IOException, ApiError, InterruptedException {
        MockResponse response = TestUtils.generateMockResponse(TestUtils.getResourceFileContent("response_subtraction.json"));
        server.enqueue(response);

        Term.Regex term1 = Term.regex("(abc|de)");
        Term.Regex term2 = Term.regex("de");

        Term result = term1.subtraction(term2);
        assertTrue(result instanceof Term.Regex);
        assertEquals("abc", ((Term.Regex) result).getPattern());

        RecordedRequest request = server.takeRequest();
        assertEquals("/api/compute/subtraction", request.getPath());

        Request.MultiTermsRequest multiTermsRequest = TestUtils.readBuffer(request.getBody(), Request.MultiTermsRequest.class);
        List<Term> terms = multiTermsRequest.getTerms();
        assertEquals(term1, terms.get(0));
        assertEquals(term2, terms.get(1));
    }

    @Test
    public void test_isEquivalentTo() throws IOException, ApiError, InterruptedException {
        MockResponse response = TestUtils.generateMockResponse(TestUtils.getResourceFileContent("response_isEquivalentTo.json"));
        server.enqueue(response);

        Term.Regex term1 = Term.regex("(abc|de)");
        Term.Fair term2 = Term.fair("rgmsW[1g2LvP=Gr&V>sLc#w-!No&(oq@Sf>X).?lI3{uh{80qWEH[#0.pHq@B-9o[LpP-a#fYI+");

        boolean result = term1.isEquivalentTo(term2);
        assertFalse(result);

        RecordedRequest request = server.takeRequest();
        assertEquals("/api/analyze/equivalence", request.getPath());

        Request.MultiTermsRequest multiTermsRequest = TestUtils.readBuffer(request.getBody(), Request.MultiTermsRequest.class);
        List<Term> terms = multiTermsRequest.getTerms();
        assertEquals(term1, terms.get(0));
        assertEquals(term2, terms.get(1));
    }

    @Test
    public void test_isSubsetOf() throws IOException, ApiError, InterruptedException {
        MockResponse response = TestUtils.generateMockResponse(TestUtils.getResourceFileContent("response_isSubsetOf.json"));
        server.enqueue(response);

        Term.Regex term1 = Term.regex("de");
        Term.Regex term2 = Term.regex("(abc|de)");

        boolean result = term1.isSubsetOf(term2);
        assertTrue(result);

        RecordedRequest request = server.takeRequest();
        assertEquals("/api/analyze/subset", request.getPath());

        Request.MultiTermsRequest multiTermsRequest = TestUtils.readBuffer(request.getBody(), Request.MultiTermsRequest.class);
        List<Term> terms = multiTermsRequest.getTerms();
        assertEquals(term1, terms.get(0));
        assertEquals(term2, terms.get(1));
    }

    @Test
    public void test_errorResponse() throws IOException {
        MockResponse response = TestUtils.generateErrorMockResponse(TestUtils.getResourceFileContent("response_error.json"), 400);
        server.enqueue(response);

        Term.Regex term1 = Term.regex("abc");
        Term.Regex term2 = Term.regex("de");

        try {
            term1.intersection(term2);
            fail();
        } catch (ApiError e) {
            assertEquals("The API returned the following error: A random error.", e.getMessage());
        }
    }
}