package com.regexsolver.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.regexsolver.api.ResponseContent;
import com.regexsolver.api.Term;

import java.util.Objects;

/**
 * Contains details about the requested {@link Term}.
 */
public final class Details implements ResponseContent {
    private final Cardinality cardinality;
    private final Length length;
    private final boolean empty;
    private final boolean total;

    /**
     * @param cardinality the number of possible values.
     * @param length      the minimum and maximum length of possible values.
     * @param empty       true if is an empty set (does not contain any value), false otherwise.
     * @param total       true if is a total set (contains all values), false otherwise.
     */
    public Details(
            @JsonProperty("cardinality") Cardinality cardinality,
            @JsonProperty("length") Length length,
            @JsonProperty("empty") boolean empty,
            @JsonProperty("total") boolean total
    ) {
        this.cardinality = cardinality;
        this.length = length;
        this.empty = empty;
        this.total = total;
    }

    /**
     * @return The number of possible values.
     */
    public Cardinality getCardinality() {
        return cardinality;
    }

    /**
     * @return The minimum and maximum length of possible values.
     */
    public Length getLength() {
        return length;
    }

    /**
     * @return true if is an empty set (does not contain any value), false otherwise.
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * @return true if is a total set (contains all values), false otherwise.
     */
    public boolean isTotal() {
        return total;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Details) obj;
        return Objects.equals(this.cardinality, that.cardinality) &&
                Objects.equals(this.length, that.length) &&
                this.empty == that.empty &&
                this.total == that.total;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardinality, length, empty, total);
    }

    @Override
    public String toString() {
        return "Details[" +
                "cardinality=" + cardinality + ", " +
                "length=" + length + ", " +
                "empty=" + empty + ", " +
                "total=" + total + ']';
    }
}
