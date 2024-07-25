package com.regexsolver.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

final class Response {
    public static final class BooleanResponse implements ResponseContent {
        private final boolean value;

        public BooleanResponse(@JsonProperty("value") boolean value) {
            this.value = value;
        }

        public boolean value() {
            return value;
        }
    }

    public static final class StringsResponse implements ResponseContent {
        private final List<String> value;

        public StringsResponse(@JsonProperty("value") List<String> value) {
            this.value = value;
        }

        public List<String> value() {
            return value;
        }
    }
}
