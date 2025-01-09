package nl.rotterdam.service.geval.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import nl.rotterdam.service.geval.AbstractTest;
import nl.rotterdam.service.geval.util.soap.XsdValidator;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

public class XsdValidatorTest extends AbstractTest {
    @Test
    void validateOkayTest() throws Exception {
        final String[] bestanden = new String[] {
                "berichten/geval-email-vraag.xml",
                "berichten/geval-emails-vraag.xml",
                "berichten/geval-email-antwoord.xml",
                "berichten/geval-emails-antwoord.xml",
        };
        XsdValidator validator = new XsdValidator("xsd/soap_1_1.xsd", "xsd/geval-operations-v1.xsd");
        for (String bestand : bestanden) {
            final String payload = getResource(bestand);
            Message<String> message = new GenericMessage<String>(payload);
            try {
                assertTrue(validator.validate(message));
            } catch (MessagingException me) {
                fail(bestand + ": " + me.getMessage());
            }
        }
    }

    @Test
    void validateErrorTest() throws Exception {
        XsdValidator validator = new XsdValidator("xsd/soap_1_1.xsd", "xsd/geval-operations-v1.xsd");

        final String payload = getResource("berichten/geval-email-vraag.xml");
        Message<String> message = new GenericMessage<String>(payload.replace("Gegeven", "gegeven"));
        String errorMessage = assertThrows(MessagingException.class, () -> validator.validate(message)).getMessage();
        assertThat(errorMessage)
                .contains("Error while validating; nested exception is org.xml.sax.SAXParseException; lineNumber: 9;")
                .contains("Invalid content was found starting with element")
                .contains(":gegeven}")
                .contains(":Gegeven");
    }
}
