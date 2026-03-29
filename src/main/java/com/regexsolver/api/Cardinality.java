package com.regexsolver.api;

import com.regexsolver.api.generated.model.CardinalityBigIntegerDto;
import com.regexsolver.api.generated.model.CardinalityDto;
import com.regexsolver.api.generated.model.CardinalityIntegerDto;
import java.util.Objects;
import java.util.Optional;

/** Base class representing the number of unique strings matched by a term. */
public abstract class Cardinality extends TermPropertiesMixin {

    private Cardinality() {}

    static Cardinality fromDto(CardinalityDto card) {
        Object inst = card.getActualInstance();
        if (inst instanceof CardinalityIntegerDto) {
            return new Cardinality.Integer(
                ((CardinalityIntegerDto) inst).getValue()
            );
        } else if (inst instanceof CardinalityBigIntegerDto) {
            return new Cardinality.BigInteger();
        } else {
            return new Cardinality.Infinite();
        }
    }

    /** Indicates that the set of matched strings is finite and exactly calculable.
     * @param value The exact count of uniquely matched strings.
     */
    public static final class Integer extends Cardinality {

        private final long value;

        public Integer(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        @Override
        public Optional<Boolean> isEmpty() {
            return Optional.of(value == 0);
        }

        @Override
        public Optional<Boolean> isEmptyString() {
            if (this.value == 1) {
                return Optional.empty();
            }
            return Optional.of(false);
        }

        @Override
        public Optional<Boolean> isTotal() {
            return Optional.of(false);
        }

        @Override
        public String toString() {
            return String.format("<Cardinality::Integer(%d)>", this.value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Integer integer = (Integer) o;
            return value == integer.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    /** Indicates that the set of matched strings is finite but too large to be returned as a standard integer. */
    public static final class BigInteger extends Cardinality {

        @Override
        public Optional<Boolean> isEmpty() {
            return Optional.of(false);
        }

        @Override
        public Optional<Boolean> isEmptyString() {
            return Optional.of(false);
        }

        @Override
        public Optional<Boolean> isTotal() {
            return Optional.of(false);
        }

        @Override
        public String toString() {
            return "<Cardinality::BigInteger>";
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof BigInteger;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    /** Indicates that the set of matched strings is infinite. */
    public static final class Infinite extends Cardinality {

        @Override
        public Optional<Boolean> isEmpty() {
            return Optional.of(false);
        }

        @Override
        public Optional<Boolean> isEmptyString() {
            return Optional.of(false);
        }

        @Override
        public Optional<Boolean> isTotal() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "<Cardinality::Infinite>";
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Infinite;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
