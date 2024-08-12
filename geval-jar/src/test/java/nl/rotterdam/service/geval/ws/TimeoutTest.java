package nl.rotterdam.service.geval.ws;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.rotterdam.service.geval.AbstractIT;
import nl.rotterdam.service.geval.TestUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integratietest voor het verifiëren van het gewenste gedrag met betrekking tot time-outs.
 * Deze test maakt gebruik van een mock-versie van de BSN validator welke 1,5 seconde gebruikt
 * per validatie. De geconfigureerde time-out waarde is ook 1,5 seconde.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
public class TimeoutTest extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Deze test stelt vast dat er een time-out responsefout wordt geretourneerd als de algehele
     * verwerkingstijd de ingestelde limietwaarde overschrijdt.
     * <p/>
     * Er worden twee controles uitgevoerd die elk zo'n 1,5 seconden in beslag nemen.
     *
     * @throws Exception
     */
    @Test
    void testTimeoutOverall() throws Exception {
        String response = mockMvc.perform(postMvc("/")
                        .contentType(MediaType.TEXT_XML)
                        .header("SOAPAction", "geval")
                        .content(getResource("berichten/geval-timeout-vraag.xml")))
                .andExpect(status().isGatewayTimeout())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
                .andReturn().getResponse().getContentAsString();

        TestUtil.assertXmlFromFileEquals("berichten/geval-timeout-antwoord.xml", response);
    }

    /**
     * Stel vast dat het mogelijk is om e-mail validaties functioneel in te perken, te degraderen,
     * als er te weinig tijd resteert om een volledige validatie d.m.v. LDAP queries uit te voeren.
     * <p/>
     * Er worden twee controles uitgevoerd. De eerste controle is een BSN controle die 1,5 seconde
     * in beslag neemt. De tweede controle is een e-mailadres controle met te weinig tijd voor een
     * volledige validatie.
     * <p/>
     * Let op! Vanwege non-deterministische race condities wordt de test een aantal keren herhaald
     * totdat het verwachte gedrag is geconstateerd.
     *
     * @throws Exception
     */
    @Test
    void testDegradatieBijTijdgebrek() throws Exception {
        for (int i=0; i < 10; i++) {
            try {
                String response = mockMvc.perform(postMvc("/")
                                .contentType(MediaType.TEXT_XML)
                                .header("SOAPAction", "geval")
                                .content(getResource("berichten/geval-timeout-degradatie-vraag.xml")))
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
                        .andReturn().getResponse().getContentAsString();

                TestUtil.assertXmlFromFileEquals("berichten/geval-timeout-degradatie-antwoord.xml", response);
                System.out.println("De test is geslaagd na " + i + " pogingen.");
                return;
            } catch (AssertionError ae) {
                System.out.println("Iteratie " + i + " is mislukt");
            }
        }
        fail("De test is mislukt ondanks herhaaldelijke pogingen");
    }
}