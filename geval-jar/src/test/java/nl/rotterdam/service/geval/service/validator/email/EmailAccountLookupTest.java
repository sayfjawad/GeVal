package nl.rotterdam.service.geval.service.validator.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Deze klasse is bedoeld voor een handmatige integratietest met IDM LDAP.
 * De TLS verbinding met de LDAP-server maakt echter gebruik van 2-zijdige authenticatie.
 * Initieel is deze klasse daarom voorzien van een annotatie met de "mock" profile omdat
 * de integratietest alleen zal slagen indien er een geldig client certificaat aanwezig is.
 * <p/>
 * Om de integratie succesvol handmatig te testen dient de annotatie met de "mock" profile
 * uitgecommentarieerd te worden en de testconfiguratie te zijn uitgebreid met de juiste
 * system property parameters:<UL>
 *     <LI>-Djavax.net.ssl.keyStore</LI>
 *     <LI>-Djavax.net.ssl.keyStorePassword</LI>
 *     <LI>-Djavax.net.ssl.trustStore</LI>
 *     <LI>-Djavax.net.ssl.trustStorePassword</LI>
 * </UL>
 */
@SpringBootTest
@ActiveProfiles("mock")
public class EmailAccountLookupTest {
    @Autowired
    private EmailAccountLookup emailAccountLookup;

    @Test
    void lookupSuccess() {
        Optional<EmailAccount> emailAccount = emailAccountLookup.findByEmailAdres("w.hu1@rotterdam.nl");
        assertTrue(emailAccount.isPresent());
        assertEquals("w.hu1@rotterdam.nl", emailAccount.get().getEmailAdres());
    }

    @Test
    void lookupFail() {
        Optional<EmailAccount> emailAccount = emailAccountLookup.findByEmailAdres("w.huX@rotterdam.nl");
        assertFalse(emailAccount.isPresent());
    }
}
