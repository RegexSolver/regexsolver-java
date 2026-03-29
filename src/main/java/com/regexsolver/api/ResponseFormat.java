package com.regexsolver.api;

import com.regexsolver.api.generated.model.ResponseOptionsDto.FormatEnum;

/**
 * Used in compute operations to specify the format of the result.
 */
public enum ResponseFormat {
    ANY,
    REGEX,
    FAIR;

    public FormatEnum toDto() {
        switch (this) {
            case ANY:
                return FormatEnum.ANY;
            case REGEX:
                return FormatEnum.REGEX;
            case FAIR:
                return FormatEnum.FAIR;
            default:
                throw new IllegalArgumentException(
                    String.format("Unsupported ResponseFormat %s.", this)
                );
        }
    }
}
