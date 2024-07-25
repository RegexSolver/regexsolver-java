package com.regexsolver.api.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Thrown when the API returns an error.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiError extends Exception {
    /**
     * Create a new instance.
     *
     * @param message The error message returned by the API.
     */
    public ApiError(@JsonProperty("message") String message) {
        super(String.format("The API returned the following error: %s", message));
    }
}
