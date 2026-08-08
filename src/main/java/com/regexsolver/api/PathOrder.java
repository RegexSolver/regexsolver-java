package com.regexsolver.api;

import com.regexsolver.api.generated.model.GenerateStringsPathOrderDto;

/**
 * Order in which the paths of the language are scheduled when generating
 * strings — the <i>shapes</i> the term allows, as opposed to the characters
 * filling them.
 */
public enum PathOrder {
    /**
     * Expand one path in full, shortest first, before moving to the next one.
     * The cheapest way to page through a whole language.
     */
    SWEEP,
    /**
     * Cover every path once before any path yields a second string. Best
     * suited to deriving test cases.
     */
    INTERLEAVE,
    /**
     * Interleave with same-length paths visited in an order drawn from the
     * seed.
     */
    SHUFFLED;

    GenerateStringsPathOrderDto toDto() {
        switch (this) {
            case SWEEP:
                return GenerateStringsPathOrderDto.SWEEP;
            case INTERLEAVE:
                return GenerateStringsPathOrderDto.INTERLEAVE;
            case SHUFFLED:
                return GenerateStringsPathOrderDto.SHUFFLED;
            default:
                throw new IllegalArgumentException(
                    String.format("Unsupported PathOrder %s.", this)
                );
        }
    }
}
