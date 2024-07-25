package com.regexsolver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okio.Buffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

class TestUtils {
    public static String getResourceFileContent(String resourceName) throws IOException {
        String content;
        try (InputStream inputStream = TestUtils.class
                .getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(inputStream);
            content = new String(inputStream.readAllBytes());
        }
        return content;
    }

    public static MockResponse generateMockResponse(String content) {
        return new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(content);
    }

    public static MockResponse generateErrorMockResponse(String content, int code) {
        return generateMockResponse(content)
                .setResponseCode(code);
    }

    public static <T> T readBuffer(Buffer buffer, Class<T> type) throws IOException {
        ByteArrayOutputStream stream
                = new ByteArrayOutputStream();
        buffer.writeTo(stream);

        String json = stream.toString();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, type);
    }
}
