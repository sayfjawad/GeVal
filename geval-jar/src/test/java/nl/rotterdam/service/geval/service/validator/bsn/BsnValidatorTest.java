package nl.rotterdam.service.geval.service.validator.bsn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nl.rotterdam.service.geval.api.v1.json.CheckResultaat;
import nl.rotterdam.service.geval.api.v1.json.Validatie;
import nl.rotterdam.service.geval.service.validator.Validator;

import org.junit.jupiter.api.Test;

/**
 * BSN validator ter illustratie van uitbreidingsmogelijkheden.
 * Een BSN is goed indien het aan de 11-proef voldoet anders niet.
 */
public class BsnValidatorTest {
    @Test
    void valideerBSN() {
        final Validator validator = new BsnValidator();
        CheckResultaat result;

        result = validator.valideer("123456782");
        assertEquals(Validatie.GOED, result.getValidatie());

        result = validator.valideer("097386649");
        assertEquals(Validatie.GOED, result.getValidatie());

        result = validator.valideer("97386649");
        assertEquals(Validatie.GOED, result.getValidatie());

        result = validator.valideer("123456781");
        assertEquals(Validatie.FOUT, result.getValidatie());

        result = validator.valideer("000000000");
        assertEquals(Validatie.FOUT, result.getValidatie());

        result = validator.valideer("003456782");
        assertEquals(Validatie.FOUT, result.getValidatie());

        result = validator.valideer("");
        assertEquals(Validatie.FOUT, result.getValidatie());
    }
}
