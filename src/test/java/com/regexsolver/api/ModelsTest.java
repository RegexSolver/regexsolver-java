package com.regexsolver.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.regexsolver.api.generated.model.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ModelsTest {

    @Test
    void testTermCreationRegex() {
        Term term = Term.regex("abc");
        assertThat(term.serialize()).isEqualTo("regex=abc");
        assertThat(term.toDto()).isNotNull();
    }

    @Test
    void testTermCreationFair() {
        Term term = Term.fair("fair_payload");
        assertThat(term.serialize()).isEqualTo("fair=fair_payload");
        assertThat(term.toDto()).isNotNull();
    }

    @Test
    void testTermFromDtoRegex() {
        TermDto genTerm = new TermDto(new TermRegexDto().value("abc"));
        Term term = Term.fromDto(genTerm);
        assertThat(term.serialize()).isEqualTo("regex=abc");
        assertThat(term.toDto()).isNotNull();
    }

    @Test
    void testTermFromDtoFair() {
        TermDto genTerm = new TermDto(new TermFairDto().value("payload"));
        Term term = Term.fromDto(genTerm);
        assertThat(term.serialize()).isEqualTo("fair=payload");
        assertThat(term.toDto()).isNotNull();
    }

    @Test
    void testCardinalityInteger() {
        Cardinality.Integer c = new Cardinality.Integer(10L);
        assertThat(c.getValue()).isEqualTo(10L);
        assertThat(c.isEmpty()).contains(false);
        assertThat(c.isEmptyString()).contains(false);
        assertThat(c.isTotal()).contains(false);
        assertThat(c.toString()).isEqualTo("<Cardinality::Integer(10)>");
    }

    @Test
    void testCardinalityFromDtoInteger() {
        CardinalityDto genCard = new CardinalityDto(
            new CardinalityIntegerDto().value(10L)
        );
        Cardinality c = Cardinality.fromDto(genCard);
        assertThat(c).isInstanceOf(Cardinality.Integer.class);
        assertThat(((Cardinality.Integer) c).getValue()).isEqualTo(10L);
    }

    @Test
    void testCardinalityFromDtoBigInteger() {
        CardinalityDto genCard = new CardinalityDto(
            new CardinalityBigIntegerDto()
        );
        Cardinality c = Cardinality.fromDto(genCard);
        assertThat(c).isInstanceOf(Cardinality.BigInteger.class);
    }

    @Test
    void testCardinalityFromDtoInfinite() {
        CardinalityDto genCard = new CardinalityDto(
            new CardinalityInfiniteDto()
        );
        Cardinality c = Cardinality.fromDto(genCard);
        assertThat(c).isInstanceOf(Cardinality.Infinite.class);
    }

    @Test
    void testCardinalityIntegerZero() {
        Cardinality.Integer c = new Cardinality.Integer(0L);
        assertThat(c.isEmpty()).contains(true);
        assertThat(c.isEmptyString()).contains(false);
    }

    @Test
    void testCardinalityIntegerOne() {
        Cardinality.Integer c = new Cardinality.Integer(1L);
        assertThat(c.isEmpty()).contains(false);
        // Equivalent to Python's `is None`
        assertThat(c.isEmptyString()).isEmpty();
    }

    @Test
    void testCardinalityBigInteger() {
        Cardinality.BigInteger c = new Cardinality.BigInteger();
        assertThat(c.isEmpty()).contains(false);
        assertThat(c.isEmptyString()).contains(false);
        assertThat(c.isTotal()).contains(false);
        assertThat(c.toString()).isEqualTo("<Cardinality::BigInteger>");
    }

    @Test
    void testCardinalityInfinite() {
        Cardinality.Infinite c = new Cardinality.Infinite();
        assertThat(c.isEmpty()).contains(false);
        assertThat(c.isEmptyString()).contains(false);
        assertThat(c.toString()).isEqualTo("<Cardinality::Infinite>");
    }

    @Test
    void testLength() {
        Length length = new Length(1, 5);
        assertThat(length.getMin()).isEqualTo(1);
        assertThat(length.getMax()).isEqualTo(5);
        assertThat(length.isEmpty()).contains(false);
        assertThat(length.isEmptyString()).contains(false);
        assertThat(length.isTotal()).contains(false);
        assertThat(length.toString()).isEqualTo("<Length: min=1, max=5>");
    }

    @Test
    void testLengthFromDto() {
        LengthDto genLen = new LengthDto();
        genLen.setMin(1);
        genLen.setMax(5);

        Length lengthObj = Length.fromDto(genLen);
        assertThat(lengthObj.getMin()).isEqualTo(1);
        assertThat(lengthObj.getMax()).isEqualTo(5);
    }

    @Test
    void testLengthEmpty() {
        Length length = new Length(null, null);
        assertThat(length.isEmpty()).contains(true);
    }

    @Test
    void testLengthEmptyString() {
        Length length = new Length(0, 0);
        assertThat(length.isEmptyString()).contains(true);
    }

    @Test
    void testLengthTotalCandidate() {
        Length length = new Length(0, null);
        assertThat(length.isTotal()).isEmpty();
    }

    @Test
    void testTermPropertiesCaching() {
        Term term = Term.regex("abc");
        assertThat(term.getCachedCardinality()).isNull();

        Cardinality.Integer c = new Cardinality.Integer(5L);
        term.setCachedCardinality(c);

        // Simulate AsyncRegexSolverClient behavior
        term.setPropertiesMixin(c);

        assertThat(term.getCachedCardinality()).isEqualTo(c);
        // Since Integer(5).isEmpty() contains false, it should set _empty to false
        assertThat(term.getCachedEmpty()).isFalse();
    }

    @Test
    void testTermGetFairAndPattern() {
        Term regexTerm = Term.regex("abc");
        assertThat(regexTerm.getPattern()).contains("abc");
        assertThat(regexTerm.getFair()).isEmpty();

        Term fairTerm = Term.fair("payload");
        assertThat(fairTerm.getFair()).contains("payload");
        assertThat(fairTerm.getPattern()).isEmpty();

        fairTerm.setCachedPattern("abc");
        assertThat(fairTerm.getPattern()).contains("abc");
    }

    @Test
    void testTermSerializeDeserialize() {
        Term term = Term.regex("abc");
        String serialized = term.serialize();
        assertThat(serialized).isEqualTo("regex=abc");
        assertThat(term.toString()).isEqualTo("regex=abc");

        Optional<Term> deserializedOpt = Term.deserialize(serialized);
        Term deserialized = deserializedOpt.get();
        assertThat(deserialized).isEqualTo(term);
        assertThat(deserialized.hashCode()).isEqualTo(term.hashCode());

        Term fairTerm = Term.fair("payload");
        assertThat(Term.deserialize(fairTerm.serialize())).isEqualTo(fairTerm);

        assertThat(Term.deserialize("invalid")).isNull();
        assertThat(Term.deserialize("unknown=value")).isNull();
    }

    @Test
    void testTermIsMatch() {
        Term term = Term.regex("a.b");
        assertThat(term.matches("axb")).isTrue();
        assertThat(term.matches("a\nb")).isTrue(); // DOTALL behavior
        assertThat(term.matches("ab")).isFalse();
        assertThat(term.matches("axxb")).isFalse(); // anchored (fullmatch)

        Term fairTerm = Term.fair("payload");
        assertThatThrownBy(() -> fairTerm.matches("abc")).isInstanceOf(
            IllegalStateException.class
        );
    }

    @Test
    void testTermRepr() {
        Term term = Term.regex("abc");
        assertThat(term.toString()).isEqualTo("<Term(type=regex, value=abc)>");
    }
}
