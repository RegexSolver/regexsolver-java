package com.regexsolver.api;

import java.util.Optional;

/**
 * A mixin providing default property inference for Term analytics.
 *
 * Returns {@code Optional.empty()} when a property cannot be strictly inferred from the current data alone.
 */
abstract class TermPropertiesMixin {

    /**
     * Infers whether the term matches no strings at all.
     *
     * @return An {@code Optional} containing {@code true} if it definitely matches no strings,
     * {@code false} if it matches at least one, or {@code Optional.empty()} if it cannot be inferred.
     */
    public Optional<Boolean> isEmpty() {
        return Optional.empty();
    }

    /**
     * Infers whether the term matches strictly the empty string ("").
     *
     * @return An {@code Optional} containing {@code true} if it definitely matches only the empty string,
     * {@code false} if it matches other strings, or {@code Optional.empty()} if it cannot be inferred.
     */
    public Optional<Boolean> isEmptyString() {
        return Optional.empty();
    }

    /**
     * Infers whether the term matches all possible strings.
     *
     * @return An {@code Optional} containing {@code true} if it definitely matches all strings,
     * {@code false} if it misses at least one string, or {@code Optional.empty()} if it cannot be inferred.
     */
    public Optional<Boolean> isTotal() {
        return Optional.empty();
    }
}
