package com.regexsolver.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

final class Request {

    public static final class MultiTermsRequest {
        private final List<Term> terms;

        public MultiTermsRequest(@JsonProperty("terms") List<Term> terms) {
            this.terms = terms;
        }

        public List<Term> getTerms() {
            return terms;
        }
    }

    public static final class GenerateStringsRequest {
        private final Term term;
        private final int count;

        public GenerateStringsRequest(
                @JsonProperty("term") Term term,
                @JsonProperty("count") int count
        ) {
            this.term = term;
            this.count = count;
        }

        public Term getTerm() {
            return term;
        }

        public int getCount() {
            return count;
        }
    }
}
