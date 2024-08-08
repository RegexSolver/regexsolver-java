package com.regexsolver.api;


import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TermSerializeTest {
    @Test
    public void test_serialization_deserialization() {
        assertSerialization(Term.regex(".*"));
        assertSerialization(Term.regex("="));
        assertSerialization(Term.regex(""));

        assertSerialization(Term.fair("rgmsW[1g2LvP=Gr&V>sLc#w-!No&(oq@Sf>X).?lI3{uh{80qWEH[#0.pHq@B-9o[LpP-a#fYI+"));
        assertSerialization(Term.fair("=rgmsW[1g2LvP=Gr&+"));
        assertSerialization(Term.fair(""));

        assertTrue(Term.deserialize(null).isEmpty());
        assertTrue(Term.deserialize("not a term").isEmpty());
    }

    private void assertSerialization(Term term) {
        String serialized = term.serialize();
        Optional<Term> deserializedOptional = Term.deserialize(serialized);
        assertTrue(deserializedOptional.isPresent());
        Term deserialized = deserializedOptional.get();
        assertEquals(term, deserialized);
    }
}