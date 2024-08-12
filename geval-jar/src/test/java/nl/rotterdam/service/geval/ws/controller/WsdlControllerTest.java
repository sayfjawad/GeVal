package nl.rotterdam.service.geval.ws.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.rotterdam.service.geval.AbstractTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Import({MockTestConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class WsdlControllerTest extends AbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getWsdlTest() throws Exception {
        String response = mockMvc.perform(get("/wsdl"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML)).andReturn().getResponse().getContentAsString();

        // Response bevat de WSDL met aangepaste padverwijzingen
        assertTrue(response.contains("targetNamespace=\"http://xmlns.rotterdam.nl/geval/v1\""));
        assertTrue(response.contains("schemaLocation=\"http://localhost:8080/wsdl?file=xsd/geval-operations-v1.xsd\""));
        assertTrue(response.contains("soap:address location=\"http://localhost:8080/wsdl\""));
    }

    @Test
    public void getSecondaryWsdlTest() throws Exception {
        String response = mockMvc.perform(get("/wsdl?file=wsdl/geval-service.wsdl"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML)).andReturn().getResponse().getContentAsString();

        assertTrue(response.contains("targetNamespace=\"http://xmlns.rotterdam.nl/geval/v1\""));
        assertTrue(response.contains("schemaLocation=\"http://localhost:8080/wsdl?file=xsd/geval-operations-v1.xsd\""));
        assertTrue(response.contains("soap:address location=\"http://localhost:8080/wsdl\""));
    }

    @Test
    public void getXsdTest() throws Exception {
        String response = mockMvc.perform(get("/wsdl?file=xsd/geval-operations-v1.xsd"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML)).andReturn().getResponse().getContentAsString();

        // Response bevat de XSD met aangepaste padverwijzingen
        assertTrue(response.contains("targetNamespace=\"http://xmlns.rotterdam.nl/geval/operations/v1\""));
        assertTrue(response.contains("schemaLocation=\"http://localhost:8080/wsdl?file=xsd/geval-v1.xsd\""));
    }
}
