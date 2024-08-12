package nl.rotterdam.service.geval.service.validator.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nl.rotterdam.service.geval.api.v1.json.CheckResultaat;
import nl.rotterdam.service.geval.api.v1.json.Validatie;
import nl.rotterdam.service.geval.api.v1.json.ValidatieType;

import org.junit.jupiter.api.Test;

public class EmailValidatorTest {
    @Test
    void testInvalideSyntaxZonderAt() {
        final EmailValidator validator = new EmailValidator();
        final CheckResultaat resultaat = validator.valideer("me-at-overheid.nl");

        assertEquals(ValidatieType.E_MAIL, resultaat.getType());
        assertEquals("me-at-overheid.nl", resultaat.getGegeven());
        assertEquals(Validatie.FOUT, resultaat.getValidatie());
        assertEquals("[INVALIDE_SYNTAX]", resultaat.getDetails().toString());
    }

    @Test
    void testInvalideSyntaxMeerdereAt() {
        final EmailValidator validator = new EmailValidator();
        final CheckResultaat resultaat = validator.valideer("me@at@overheid.nl");

        assertEquals(ValidatieType.E_MAIL, resultaat.getType());
        assertEquals("me@at@overheid.nl", resultaat.getGegeven());
        assertEquals(Validatie.FOUT, resultaat.getValidatie());
        assertEquals("[INVALIDE_SYNTAX]", resultaat.getDetails().toString());
    }

    @Test
    void testInvalideDomeinSyntax() {
        final EmailValidator validator = new EmailValidator();
        final CheckResultaat resultaat = validator.valideer("me@over+heid.nl");

        assertEquals(ValidatieType.E_MAIL, resultaat.getType());
        assertEquals("me@over+heid.nl", resultaat.getGegeven());
        assertEquals(Validatie.FOUT, resultaat.getValidatie());
        assertEquals("[INVALIDE_SYNTAX_DOMEIN]", resultaat.getDetails().toString());
    }

    @Test
    void testInvalideGebruikerSyntax() {
        final EmailValidator validator = new EmailValidator();
        final CheckResultaat resultaat = validator.valideer("@overheid.nl");

        assertEquals(ValidatieType.E_MAIL, resultaat.getType());
        assertEquals("@overheid.nl", resultaat.getGegeven());
        assertEquals(Validatie.FOUT, resultaat.getValidatie());
        assertEquals("[INVALIDE_SYNTAX_GEBRUIKER]", resultaat.getDetails().toString());
    }

    /**
     * Stel vast dat er in geval van een validatiefout meerdere validatiedetails geretourneerd kunnen worden.
     */
    @Test
    void testInvalideGebruikerEnDomeinSyntax() {
        final EmailValidator validator = new EmailValidator();
        final CheckResultaat resultaat = validator.valideer("@over+heid.nl");

        assertEquals(ValidatieType.E_MAIL, resultaat.getType());
        assertEquals("@over+heid.nl", resultaat.getGegeven());
        assertEquals(Validatie.FOUT, resultaat.getValidatie());
        assertEquals("[INVALIDE_SYNTAX_DOMEIN, INVALIDE_SYNTAX_GEBRUIKER]", resultaat.getDetails().toString());
    }

    @Test
    void testDomeinBestaatNiet() {
        final EmailValidator validator = new EmailValidator();
        final CheckResultaat resultaat = validator.valideer("me@over-789-heid.nl");

        assertEquals(ValidatieType.E_MAIL, resultaat.getType());
        assertEquals("me@over-789-heid.nl", resultaat.getGegeven());
        assertEquals(Validatie.FOUT, resultaat.getValidatie());
        assertEquals("[DOMEIN_BESTAAT_NIET]", resultaat.getDetails().toString());
    }

    @Test
    void testDomeinZonderEmail() {
        final EmailValidator validator = new EmailValidator();
        final CheckResultaat resultaat = validator.valideer("me@rotterdam.xyz");

        assertEquals(ValidatieType.E_MAIL, resultaat.getType());
        assertEquals("me@rotterdam.xyz", resultaat.getGegeven());
        assertEquals(Validatie.FOUT, resultaat.getValidatie());
        assertEquals("[DOMEIN_ZONDER_EMAIL]", resultaat.getDetails().toString());
    }

    @Test
    void testDomeinMetEmail() {
        final EmailValidator validator = new EmailValidator();
        final CheckResultaat resultaat = validator.valideer("me@overheid.nl");

        assertEquals(ValidatieType.E_MAIL, resultaat.getType());
        assertEquals("me@overheid.nl", resultaat.getGegeven());
        assertEquals(Validatie.GOED, resultaat.getValidatie());
        assertEquals("[DOMEIN_MET_EMAIL]", resultaat.getDetails().toString());
        assertNull(resultaat.getToelichting());
    }

    @Test
    void testDomeinAccepteertEmails() {
        final EmailValidator validator = new EmailValidator();
        final CheckResultaat resultaat = validator.valideer("me@overheid.nl");

        assertEquals(ValidatieType.E_MAIL, resultaat.getType());
        assertEquals("me@overheid.nl", resultaat.getGegeven());
        assertEquals(Validatie.GOED, resultaat.getValidatie());
        assertEquals("[DOMEIN_MET_EMAIL]", resultaat.getDetails().toString());
    }

    @Test
    void testEmailSyntax() {
       final EmailValidator validator = new EmailValidator();
        assertTrue(validator.checkSyntax("me@rotterdam.nl"));
        assertTrue(validator.checkSyntax("g.e.bruiker@rotterdam.nl"));
        assertTrue(validator.checkSyntax("g.e.bruiker@rotterdam.xyz"));
        assertTrue(validator.checkSyntax("123456@rotterdam.nl"));
        assertTrue(validator.checkSyntax("123+456@rotterdam.nl"));
        assertTrue(validator.checkSyntax("123+456+@rotterdam.nl"));
        assertTrue(validator.checkSyntax("123456@[193.176.221.230]"));
        assertTrue(validator.checkSyntax("123456@[2001:0db8:85a3:0000:0000:8a2e:0370:7334]"));

        assertFalse(validator.checkSyntax("me.rotterdam.nl"));
        assertFalse(validator.checkSyntax("me..@rotterdam.nl"));
        assertFalse(validator.checkSyntax("me..@@rotterdam.nl"));
        assertFalse(validator.checkSyntax("g.e.bruiker@rotterdam"));
        assertFalse(validator.checkSyntax("123456@193.176.221.230"));
        assertFalse(validator.checkSyntax("123456@[+++++++]"));
    }

    @Test
    void testDomainSyntax() {
        final EmailValidator validator = new EmailValidator();
        assertTrue(validator.checkDomainSyntax("rotterdam.nl"));
        assertTrue(validator.checkDomainSyntax("rotterdam.xyz"));

        // Syntactisch okay maar niet bestaand
        assertTrue(validator.checkDomainSyntax("rtttrdm.nl"));
        assertTrue(validator.checkDomainSyntax("rt82345311113trdm.uk"));

        assertFalse(validator.checkDomainSyntax("123.12.13.201"));
        assertTrue(validator.checkDomainSyntax("[123.12.13.201]"));
    }

    @Test
    void testDomainExists() {
        final EmailValidator validator = new EmailValidator();
        assertTrue(validator.checkDomainExists("rotterdam.nl"));

        assertTrue(validator.checkDomainExists("rotterdam.xyz"));

        // Er is wel een DNS SOA-record maar geen IP adres
        //  rtttrdm.nl	SOA	0 dns1.nic.uk hostmaster@nic.uk
        assertFalse(validator.checkDomainExists("rt82345311113trdm.uk"));

        // Er is wel een DNS SOA-record maar geen IP adres
        //  rtttrdm.nl	SOA	0 ns1.dns.nl hostmaster@domain-registry.nl
        assertFalse(validator.checkDomainExists("rtttrdm.nl"));
    }

    @Test
    void testDomainHasMX() {
        final EmailValidator validator = new EmailValidator();
        assertTrue(validator.checkDomainHasMX("rotterdam.nl"));

        // Domein bestaat maar heeft geen MX-record
        assertFalse(validator.checkDomainHasMX("rotterdam.xyz"));
    }
}
