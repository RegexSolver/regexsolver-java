package com.regexsolver.api;

import com.regexsolver.api.dto.Cardinality;
import com.regexsolver.api.dto.Length;
import com.regexsolver.api.exception.ApiError;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class IntegrationTest {
    @Before
    public void setUp() throws Exception {
        RegexSolver.initialize();
    }

    // Analyze

    @Test
    public void test_analyze_cardinality() throws Exception {
        Term term = Term.regex("[0-4]");

        Cardinality cardinality = term.getCardinality();
        assertEquals("Integer(5)", cardinality.toString());
    }

    @Test
    public void test_analyze_dot() throws Exception {
        Term term = Term.regex("(abc|de)");
        String dot = term.getDot();
        assertTrue(dot.startsWith("digraph "));
    }

    @Test
    public void test_analyze_empty_string() throws Exception {
        Term term = Term.regex("");
        boolean result = term.isEmptyString();
        assertTrue(result);
    }

    @Test
    public void test_analyze_empty() throws Exception {
        Term term = Term.regex("[]");
        boolean result = term.isEmpty();
        assertTrue(result);
    }

    @Test
    public void test_analyze_total() throws Exception {
        Term term = Term.regex(".*");
        boolean result = term.isTotal();
        assertTrue(result);
    }

    @Test
    public void test_analyze_equivalent() throws Exception {
        Term term1 = Term.regex("(abc|de)");
        Term term2 = Term.fair(
                "<uw$8AJYkaU].HFn1kT[tx*-VAZ8usSKXcEKZ[wx:F8vYuR-b?tFFk1eM2RXs9yuu5dakz7r/{!AW9/(hK0]knHS&Q]!@K=ahmGr1Dbjb5(XE1UT%Ab@8rXvYop}$");
        boolean result = term1.equivalent(term2);
        assertFalse(result);
    }

    @Test
    public void test_analyze_length_empty() throws Exception {
        Term term = Term.regex("[]");
        Length length = term.getLength();
        assertEquals("Length[minimum=null, maximum=null]", length.toString());
    }

    @Test
    public void test_analyze_length() throws Exception {
        Term term = Term.regex("(abc)?");
        Length length = term.getLength();
        assertEquals("Length[minimum=0, maximum=3]", length.toString());
    }

    @Test
    public void test_analyze_pattern() throws Exception {
        Term term = Term.regex("abc.*");
        String pattern = term.getPattern();
        assertEquals("abc.*", pattern);
    }

    @Test
    public void test_analyze_subset() throws Exception {
        Term term1 = Term.regex("de");
        Term term2 = Term.regex("(abc|de)");
        boolean result = term1.subset(term2);
        assertTrue(result);
    }

    // Compute

    @Test
    public void test_compute_concat() throws Exception {
        Term term1 = Term.regex("abc");
        Term term2 = Term.regex("de");
        OperationOptions operationOptions = OperationOptions.newDefault()
                .responseFormat(ResponseFormat.REGEX);
        Term result = term1.concat(operationOptions, term2);
        assertEquals("regex=abcde", result.toString());
    }

    @Test
    public void test_compute_difference() throws Exception {
        Term term1 = Term.regex("(abc|de)");
        Term term2 = Term.regex("de");
        OperationOptions operationOptions = OperationOptions.newDefault()
                .responseFormat(ResponseFormat.REGEX);
        Term result = term1.difference(operationOptions, term2);
        assertEquals("regex=abc", result.toString());
    }

    @Test
    public void test_compute_intersection() throws Exception {
        Term term1 = Term.regex("(abc|de){2}");
        Term term2 = Term.regex("de.*");
        Term term3 = Term.regex(".*abc");
        OperationOptions operationOptions = OperationOptions.newDefault()
                .responseFormat(ResponseFormat.REGEX);
        Term result = term1.intersection(operationOptions, term2, term3);
        assertEquals("regex=deabc", result.toString());
    }

    @Test
    public void test_compute_repeat() throws Exception {
        Term term = Term.regex("abc");
        OperationOptions operationOptions = OperationOptions.newDefault()
                .responseFormat(ResponseFormat.REGEX);
        Term result = term.repeat(operationOptions, 3, 5);
        assertEquals("regex=(abc){3,5}", result.toString());
    }

    @Test
    public void test_compute_union() throws Exception {
        Term term1 = Term.regex("abc");
        Term term2 = Term.regex("de");
        Term term3 = Term.regex("fghi");
        OperationOptions operationOptions = OperationOptions.newDefault()
                .responseFormat(ResponseFormat.REGEX);
        Term result = term1.union(operationOptions, term2, term3);
        assertEquals("regex=(abc|de|fghi)", result.toString());
    }

    // Generate

    @Test
    public void test_generate_strings() throws Exception {
        Term term = Term.regex("(abc|de){2}");
        List<String> strings = term.generateStrings(10);
        assertEquals(4, strings.size());
    }

    // README

    @Test
    public void test_readme_quickstart() throws Exception {
        Term term1 = Term.regex("(abc|de|fg){2,}");
        Term term2 = Term.regex("de.*");
        Term term3 = Term.regex(".*abc");

        Term result = term1.intersection(term2, term3)
                .difference(Term.regex(".+(abc|de).+"));
        assertEquals("de(fg)*abc", result.getPattern());
    }

    @Test
    public void test_readme_response_format() throws Exception {
        Term term = Term.regex("abcde");

        OperationOptions operationOptions = OperationOptions.newDefault()
                .responseFormat(ResponseFormat.REGEX);
        Term result1 = term.union(operationOptions, Term.regex("de"));

        assertEquals("regex=(abc)?de", result1.toString());

        operationOptions = OperationOptions.newDefault()
                .responseFormat(ResponseFormat.FAIR);
        Term result2 = term.union(operationOptions, Term.regex("de"));

        assertTrue(result2.toString().startsWith("fair="));
    }

    @Test
    public void test_readme_execution_timeout() throws Exception {
        try {
            Term term1 = Term.regex(".*ab.*c(de|fg).*dab.*c(de|fg).*ab.*c(de|fg).*dab.*c");
            Term term2 = Term.regex(".*abc.*");

            OperationOptions operationOptions = OperationOptions.newDefault()
                    .executionTimeout(5);
            term1.difference(operationOptions, term2);
        } catch (ApiError e) {
            System.out.println(e.getMessage());
        }
    }
}