package com.regexsolver.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.regexsolver.api.Response.BooleanResponse;
import com.regexsolver.api.Response.StringsResponse;
import com.regexsolver.api.dto.Details;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Term.Fair.class, name = "fair"),
        @JsonSubTypes.Type(value = Term.Regex.class, name = "regex"),
        @JsonSubTypes.Type(value = Details.class, name = "details"),
        @JsonSubTypes.Type(value = StringsResponse.class, name = "strings"),
        @JsonSubTypes.Type(value = BooleanResponse.class, name = "boolean"),
})
public interface ResponseContent {
}
