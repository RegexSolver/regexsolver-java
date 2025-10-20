package com.regexsolver.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

final class Request {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static final class RequestOptions {
        private final ResponseOptions response;
        private final ExecutionOptions execution;

        public static RequestOptions fromArgs(
                ResponseFormat format,
                Integer timeout) {

            ResponseOptions response = null;
            if (format != null) {
                response = new ResponseOptions(format);
            }

            ExecutionOptions execution = null;
            if (timeout != null) {
                execution = new ExecutionOptions(timeout);
            }

            if (response == null && execution == null) {
                return null;
            } else {
                return new RequestOptions(response, execution);
            }
        }

        public RequestOptions(
                @JsonProperty("response") ResponseOptions response,
                @JsonProperty("execution") ExecutionOptions execution) {
            this.response = response;
            this.execution = execution;
        }

        public int getSchemaVersion() {
            return 1;
        }

        public ResponseOptions getResponse() {
            return response;
        }

        public ExecutionOptions getExecution() {
            return execution;
        }

        public static final class ResponseOptions {
            private final ResponseFormat format;

            public ResponseOptions(@JsonProperty("format") ResponseFormat format) {
                this.format = format;
            }

            public ResponseFormat getFormat() {
                return format;
            }
        }

        public static final class ExecutionOptions {
            private final Integer timeout;

            public ExecutionOptions(@JsonProperty("timeout") Integer timeout) {
                this.timeout = timeout;
            }

            public Integer getTimeout() {
                return timeout;
            }
        }

        public enum ResponseFormat {
            @JsonProperty("any")
            ANY,
            @JsonProperty("regex")
            REGEX,
            @JsonProperty("fair")
            FAIR
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static final class MultiTermsRequest {
        private final List<Term> terms;
        private final RequestOptions options;

        public MultiTermsRequest(@JsonProperty("terms") List<Term> terms,
                @JsonProperty("options") RequestOptions options) {
            this.terms = terms;
            this.options = options;
        }

        public List<Term> getTerms() {
            return terms;
        }

        public RequestOptions getOptions() {
            return options;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static final class RepeatRequest {
        private final Term term;
        private final int min;
        private final Integer max;
        private final RequestOptions options;

        public RepeatRequest(
                @JsonProperty("term") Term term,
                @JsonProperty("min") int min,
                @JsonProperty("max") Integer max,
                @JsonProperty("options") RequestOptions options) {
            this.term = term;
            this.min = min;
            this.max = max;
            this.options = options;
        }

        public Term getTerm() {
            return term;
        }

        public int getMin() {
            return min;
        }

        public Integer getMax() {
            return max;
        }

        public RequestOptions getOptions() {
            return options;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static final class GenerateStringsRequest {
        private final Term term;
        private final int count;
        private final RequestOptions options;

        public GenerateStringsRequest(
                @JsonProperty("term") Term term,
                @JsonProperty("count") int count,
                @JsonProperty("options") RequestOptions options) {
            this.term = term;
            this.count = count;
            this.options = options;
        }

        public Term getTerm() {
            return term;
        }

        public int getCount() {
            return count;
        }

        public RequestOptions getOptions() {
            return options;
        }
    }
}
