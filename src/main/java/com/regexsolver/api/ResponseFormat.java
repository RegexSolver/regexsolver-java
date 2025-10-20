package com.regexsolver.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ResponseFormat {
    @JsonProperty("any")
    ANY,
    @JsonProperty("regex")
    REGEX,
    @JsonProperty("fair")
    FAIR
}
