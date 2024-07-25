package com.regexsolver.api.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * Contains the minimum and maximum length of possible values.
 */
@JsonDeserialize(using = Length.LengthDeserializer.class)
public final class Length {
    private final Long minimum;
    private final Long maximum;

    /**
     * @param minimum the minimum length of possible values, empty if is an empty set.
     * @param maximum the maximum length of possible values, empty if the maximum length is infinite or if is an empty set.
     */
    Length(Long minimum, Long maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * @return The minimum length of possible values, empty if is an empty set.
     */
    public OptionalLong getMinimum() {
        if (minimum == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(minimum);
    }

    /**
     * @return The maximum length of possible values, empty if the maximum length is infinite or if is an empty set.
     */
    public OptionalLong getMaximum() {
        if (maximum == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(maximum);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Length) obj;
        return Objects.equals(this.minimum, that.minimum) &&
                Objects.equals(this.maximum, that.maximum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minimum, maximum);
    }

    @Override
    public String toString() {
        return "Length[" +
                "minimum=" + minimum + ", " +
                "maximum=" + maximum + ']';
    }

    static class LengthDeserializer extends JsonDeserializer<Length> {
        @Override
        public Length deserialize(JsonParser jp, DeserializationContext ctx)
                throws IOException {
            Long[] lengthArray = jp.readValueAs(Long[].class);
            if (lengthArray != null && lengthArray.length == 2) {
                return new Length(
                        lengthArray[0],
                        lengthArray[1]
                );
            } else {
                throw new IOException("Invalid length array.");
            }
        }
    }
}