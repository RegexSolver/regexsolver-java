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

    /** The shortest possible matched string length, or {@link Optional::empty} if the language is empty. */
    public Optional<Integer> getMin() {
        return Optional.ofNullable(min);
    }

    /** The longest possible matched string length, or {@link Optional::empty} if the length is unbounded. */
    public Optional<Integer> getMax() {
        return Optional.ofNullable(max);
    }

    @Override
    public Optional<Boolean> isEmpty() {
        return Optional.of(this.min == null && this.max == null);
    }

    @Override
    public Optional<Boolean> isEmptyString() {
        return Optional.of(this.min == 0 && this.max == 0);
    }

    @Override
    public Optional<Boolean> isTotal() {
        if (this.min != 0 || this.max != null) {
            return Optional.of(false);
        } else {
            return Optional.empty();
        }
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
