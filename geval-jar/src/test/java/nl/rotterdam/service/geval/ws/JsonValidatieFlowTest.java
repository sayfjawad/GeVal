package nl.rotterdam.service.geval.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * Systeemtest voor 'generieke validatie' op basis van JSON request.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class JsonValidatieFlowTest extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRequestResponseOkay() throws Exception {
        String response = mockMvc.perform(postMvc("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getResource("berichten/geval-email-vraag.json")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        TestUtil.assertJsonFromFileEquals(isAccountLookupAvailable() ?
                "berichten/geval-email-antwoord-met-account-lookup.json" : "berichten/geval-email-antwoord.json", response);
    }

    @Test
    void testMeerdereValidaties() throws Exception {
        String response = mockMvc.perform(postMvc("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getResource("berichten/geval-emails-vraag.json")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        TestUtil.assertJsonFromFileEquals(isAccountLookupAvailable() ?
                "berichten/geval-emails-antwoord-met-account-lookup.json" : "berichten/geval-emails-antwoord.json", response);
    }

    @Test
    void testTeveelValidaties() throws Exception {
        String response = mockMvc.perform(postMvc("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getResource("berichten/geval-teveel-vraag.json")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn().getResponse().getContentAsString();

        TestUtil.assertJsonFromFileEquals("berichten/geval-teveel-antwoord.json", response);
    }

    @Test
    void testGeenValidaties() throws Exception {
        String response = mockMvc.perform(postMvc("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getResource("berichten/geval-geen-vraag.json")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn().getResponse().getContentAsString();

        TestUtil.assertJsonFromFileEquals("berichten/geval-geen-antwoord.json", response);
    }

    @Test
    void clientFoutResponse() throws Exception {
        String payloadMetFout = getResource("berichten/geval-email-vraag.json").replace("gegeven", "data");
        String response = mockMvc.perform(postMvc("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadMetFout))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn().getResponse().getContentAsString();

        TestUtil.assertJsonFromFileEquals("berichten/geval-fout-antwoord.json", response.replace("\\r\\n", "\\n"));
    }
}