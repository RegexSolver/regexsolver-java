package com.regexsolver.api;

import com.regexsolver.api.generated.model.LengthDto;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the minimum and maximum lengths of any string matched by the term.
 */
public final class Length extends TermPropertiesMixin {

    private final Integer min;
    private final Integer max;

    public Length(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    static Length fromDto(LengthDto len) {
        return new Length(len.getMin(), len.getMax());
    }

    /** The shortest possible matched string length, or {@link java.util.Optional#empty()} if the language is empty. */
    public Optional<Integer> getMin() {
        return Optional.ofNullable(min);
    }

    /** The longest possible matched string length, or {@link java.util.Optional#empty()} if the length is unbounded. */
    public Optional<Integer> getMax() {
        return Optional.ofNullable(max);
    }

    @Override
    public Optional<Boolean> isEmpty() {
        return Optional.of(this.min == null && this.max == null);
    }

    @Override
    public Optional<Boolean> isEmptyString() {
        // min/max are null for the empty language and max is null when the length
        // is unbounded, so these comparisons must not unbox.
        return Optional.of(isZero(this.min) && isZero(this.max));
    }

    @Override
    public Optional<Boolean> isTotal() {
        if (!isZero(this.min) || this.max != null) {
            return Optional.of(false);
        } else {
            return Optional.empty();
        }
    }

    private static boolean isZero(Integer value) {
        return value != null && value.intValue() == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Length length = (Length) o;
        return (
            Objects.equals(min, length.min) && Objects.equals(max, length.max)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return String.format(
            "<Length: min=%s, max=%s>",
            min == null ? "null" : min,
            max == null ? "null" : max
        );
    }
}
