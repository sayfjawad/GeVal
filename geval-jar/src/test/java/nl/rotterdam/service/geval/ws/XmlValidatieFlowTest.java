package nl.rotterdam.service.geval.ws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.rotterdam.service.geval.AbstractIT;
import nl.rotterdam.service.geval.TestUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Systeemtest voor uitvoering van 'generieke validatie' op basis van XML.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class XmlValidatieFlowTest extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testValidatieOkay() throws Exception {
        String response = mockMvc.perform(postMvc("/")
                        .contentType(MediaType.TEXT_XML)
                        .header("SOAPAction", "geval")
                        .content(getResource("berichten/geval-email-vraag.xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
                .andReturn().getResponse().getContentAsString();

        TestUtil.assertXmlFromFileEquals(isAccountLookupAvailable() ?
                "berichten/geval-email-antwoord-met-account-lookup.xml" : "berichten/geval-email-antwoord.xml", response);
    }

    @Test
    void testMeerdereValidatieOkay() throws Exception {
        String response = mockMvc.perform(postMvc("/")
                        .contentType(MediaType.TEXT_XML)
                        .header("SOAPAction", "geval")
                        .content(getResource("berichten/geval-emails-vraag.xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
                .andReturn().getResponse().getContentAsString();

        TestUtil.assertXmlFromFileEquals(isAccountLookupAvailable() ?
                "berichten/geval-emails-antwoord-met-account-lookup.xml" : "berichten/geval-emails-antwoord.xml", response);
    }

    @Test
    void clientSOAPFaultResponse() throws Exception {
        String payloadMetFout = getResource("berichten/geval-email-vraag.xml").replace("Gegeven", "gegeven");
        String response = mockMvc.perform(postMvc("/")
                        .contentType(MediaType.TEXT_XML)
                        .header("SOAPAction", "geval")
                        .content(payloadMetFout))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
                .andReturn().getResponse().getContentAsString();

        assertThat(response)
                .contains("Invalid content was found starting with element")
                .contains(":gegeven}")
                .contains(":Gegeven");
    }
}