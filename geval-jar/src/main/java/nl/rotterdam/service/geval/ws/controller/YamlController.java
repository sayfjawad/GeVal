package nl.rotterdam.service.geval.ws.controller;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YamlController {
    @RequestMapping(value = "/yaml", produces = { "application/x-yaml"})
    public String getYaml() throws IOException {
        final var in = this.getClass().getClassLoader().getResourceAsStream("api/openapi.yaml");
        return IOUtils.toString(in, "UTF-8");
    }
}
