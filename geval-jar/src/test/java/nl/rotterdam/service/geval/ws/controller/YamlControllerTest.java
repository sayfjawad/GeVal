package nl.rotterdam.service.geval.ws.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.rotterdam.service.geval.AbstractTest;
import nl.rotterdam.service.geval.TestUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import({MockTestConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class YamlControllerTest extends AbstractTest {
    private final String BASE_PATH = "/yaml";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getYamlTest() throws Exception {
        String succesResponse = mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/x-yaml"))
                .andReturn().getResponse().getContentAsString();

        assertEquals(
                TestUtil.readResourceAsString("api/openapi.yaml", "UTF-8").replace("\r\n","\n"),
                succesResponse.replace("\r\n","\n"));
    }
}

