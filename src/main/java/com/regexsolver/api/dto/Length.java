package com.regexsolver.api.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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
     * @param minimum the minimum length of possible values, empty if is an empty
     *                set.
     * @param maximum the maximum length of possible values, empty if the maximum
     *                length is infinite or if is an empty set.
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
     * @return The maximum length of possible values, empty if the maximum length is
     *         infinite or if is an empty set.
     */
    public OptionalLong getMaximum() {
        if (maximum == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(maximum);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
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
            JsonToken t = jp.currentToken();
            if (t == null)
                t = jp.nextToken();

            if (t == JsonToken.START_ARRAY) {
                Long[] arr = jp.readValueAs(Long[].class);
                if (arr == null || arr.length != 2) {
                    throw new IOException("Expected [minimum,maximum] array.");
                }
                return new Length(arr[0], arr[1]);
            }

            if (t == JsonToken.START_OBJECT) {
                Long min = null;
                Long max = null;

                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    String field = jp.currentName();
                    jp.nextToken(); // move to value
                    if ("min".equals(field)) {
                        min = jp.currentToken() == JsonToken.VALUE_NULL ? null : jp.getLongValue();
                    } else if ("max".equals(field)) {
                        max = jp.currentToken() == JsonToken.VALUE_NULL ? null : jp.getLongValue();
                    } else {
                        jp.skipChildren(); // ignore unknown fields
                    }
                }
                return new Length(min, max);
            }

            if (t == JsonToken.VALUE_NULL) {
                return null;
            }

            throw new IOException("Expected [minimum,maximum] array, or {minimum,maximum} object.");
        }
    }
}