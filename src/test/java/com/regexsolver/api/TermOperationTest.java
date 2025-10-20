package com.regexsolver.api;

import com.regexsolver.api.exception.ApiError;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

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