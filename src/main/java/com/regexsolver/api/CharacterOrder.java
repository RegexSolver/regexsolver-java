package com.regexsolver.api;

import com.regexsolver.api.generated.model.GenerateStringsCharacterOrderDto;

/**
 * Order in which the strings within each path are produced when generating
 * strings. Orthogonal to {@link PathOrder}: it does not change <i>what</i>
 * can be generated, only which strings are reached first.
 */
public enum CharacterOrder {
    /**
     * Expand each position from the low end of its character range first — a
     * stable order returning the smallest witnesses of a path first.
     */
    ASCENDING,
    /**
     * A permutation drawn from the seed, so the strings look like real
     * inputs. Random in look only — generation stays reproducible.
     */
    SHUFFLED;

    GenerateStringsCharacterOrderDto toDto() {
        switch (this) {
            case ASCENDING:
                return GenerateStringsCharacterOrderDto.ASCENDING;
            case SHUFFLED:
                return GenerateStringsCharacterOrderDto.SHUFFLED;
            default:
                throw new IllegalArgumentException(
                    String.format("Unsupported CharacterOrder %s.", this)
                );
        }
    }
}
