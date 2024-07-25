package com.regexsolver.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract class that represent the number of possible values.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Cardinality.BigInteger.class, name = "BigInteger"),
        @JsonSubTypes.Type(value = Cardinality.Infinite.class, name = "Infinite"),
        @JsonSubTypes.Type(value = Cardinality.Integer.class, name = "Integer")
})
public abstract class Cardinality {
    /**
     * @return true if it has a finite number of values, false otherwise.
     */
    public abstract boolean isFinite();

    public abstract String toString();

    /**
     * An infinite number of possible values.
     */
    public static final class Infinite extends Cardinality {
        @Override
        public boolean isFinite() {
            return false;
        }

        @Override
        public String toString() {
            return "Infinite";
        }
    }

    /**
     * A finite number of possible values, but the number is too big to be computed.
     */
    public static final class BigInteger extends Cardinality {
        @Override
        public boolean isFinite() {
            return true;
        }

        @Override
        public String toString() {
            return "BigInteger";
        }
    }

    /**
     * A finite number of possible values, available in {@link #getCount()}.
     */
    public static final class Integer extends Cardinality {
        private final long count;

        /**
         * Create a new instance.
         *
         * @param count The number of possible values.
         */
        public Integer(@JsonProperty("value") long count) {
            this.count = count;
        }

        @Override
        public boolean isFinite() {
            return false;
        }

        /**
         * @return The number of possible values.
         */
        public long getCount() {
            return count;
        }

        @Override
        public String toString() {
            return String.format("Integer(%s)", count);
        }
    }
}
