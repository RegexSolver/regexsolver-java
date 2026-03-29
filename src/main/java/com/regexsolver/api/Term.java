package com.regexsolver.api;

import com.regexsolver.api.generated.model.TermDto;
import com.regexsolver.api.generated.model.TermFairDto;
import com.regexsolver.api.generated.model.TermRegexDto;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a mathematical term (Regex or FAIR) on which operations can be performed.
 */
public abstract class Term {

    private final String value;

    // Shared Cache (Internal)
    private Cardinality cardinality;
    private Length length;
    private Boolean empty;
    private Boolean emptyString;
    private Boolean total;
    protected String pattern;
    private String dot;
    private Term stableTerm;

    private Pattern compiledRegex;

    protected Term(String value) {
        this.value = value;
    }

    public abstract Optional<String> getPattern();

    public abstract Optional<String> getFair();

    abstract TermDto toDto();

    public static Term regex(String pattern) {
        return new RegexTerm(pattern);
    }

    public static Term fair(String payload) {
        return new FairTerm(payload);
    }

    // --- Shared Behavior ---

    public String getValue() {
        return value;
    }

    void setPropertiesMixin(TermPropertiesMixin propertiesMixin) {
        propertiesMixin.isEmpty().ifPresent(this::setCachedEmpty);
        propertiesMixin.isEmptyString().ifPresent(this::setCachedEmptyString);
        propertiesMixin.isTotal().ifPresent(this::setCachedTotal);
    }

    /**
     * Client-side matching implementation.
     * @param str The string to test against the term.
     * @return True if matches, false if not. Throws if pattern is not set.
     */
    public boolean isMatch(String str) {
        Optional<String> patternOpt = getPattern();
        if (patternOpt.isEmpty()) {
            throw new IllegalStateException(
                "The regex pattern of this term is not defined yet, call getPattern() on the client to set it."
            );
        }

        if (compiledRegex == null) {
            compiledRegex = Pattern.compile(patternOpt.get(), Pattern.DOTALL);
        }

        return compiledRegex.matcher(str).matches();
    }

    public abstract String serialize();

    public static Optional<Term> deserialize(String serialized) {
        if (serialized == null || !serialized.contains("=")) {
            return Optional.empty();
        }

        int index = serialized.indexOf("=");
        String typeStr = serialized.substring(0, index);
        String val = serialized.substring(index + 1);

        if ("regex".equalsIgnoreCase(typeStr)) {
            return Optional.of(regex(val));
        } else if ("fair".equalsIgnoreCase(typeStr)) {
            return Optional.of(fair(val));
        }
        return Optional.empty();
    }

    static Term fromDto(TermDto dto) {
        Object instance = dto.getActualInstance();
        if (instance instanceof TermRegexDto) {
            return Term.regex(((TermRegexDto) instance).getValue());
        } else {
            return Term.fair(((TermFairDto) instance).getValue());
        }
    }

    // --- Shared Getters/Setters ---

    Cardinality getCachedCardinality() {
        return cardinality;
    }

    void setCachedCardinality(Cardinality cardinality) {
        setPropertiesMixin(cardinality);
        this.cardinality = cardinality;
    }

    Length getCachedLength() {
        return length;
    }

    void setCachedLength(Length length) {
        setPropertiesMixin(length);
        this.length = length;
    }

    Boolean getCachedEmpty() {
        return empty;
    }

    void setCachedEmpty(Boolean empty) {
        this.empty = empty;
    }

    Boolean getCachedEmptyString() {
        return emptyString;
    }

    void setCachedEmptyString(Boolean emptyString) {
        this.emptyString = emptyString;
    }

    Boolean getCachedTotal() {
        return total;
    }

    void setCachedTotal(Boolean total) {
        this.total = total;
    }

    void setCachedPattern(String pattern) {
        this.pattern = pattern;
    }

    String getCachedDot() {
        return dot;
    }

    void setCachedDot(String dot) {
        this.dot = dot;
    }

    Term getCachedStableTerm() {
        return stableTerm;
    }

    void setCachedStableTerm(Term stableTerm) {
        this.stableTerm = stableTerm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term = (Term) o;
        return Objects.equals(serialize(), term.serialize());
    }

    @Override
    public int hashCode() {
        return serialize().hashCode();
    }

    @Override
    public String toString() {
        return serialize();
    }

    public static final class RegexTerm extends Term {

        RegexTerm(String value) {
            super(value);
        }

        @Override
        public Optional<String> getPattern() {
            return Optional.of(getValue());
        }

        @Override
        public Optional<String> getFair() {
            return Optional.ofNullable(getCachedStableTerm()).map(t ->
                t.getFair().orElse(null)
            );
        }

        @Override
        TermDto toDto() {
            return new TermDto(new TermRegexDto().value(getValue()));
        }

        @Override
        public String serialize() {
            return "regex=" + getValue();
        }
    }

    public static final class FairTerm extends Term {

        FairTerm(String value) {
            super(value);
        }

        @Override
        public Optional<String> getPattern() {
            return Optional.ofNullable(this.pattern);
        }

        @Override
        public Optional<String> getFair() {
            return Optional.of(getValue());
        }

        @Override
        TermDto toDto() {
            return new TermDto(new TermFairDto().value(getValue()));
        }

        @Override
        public String serialize() {
            return "fair=" + getValue();
        }
    }
}
