package nl.rotterdam.service.geval;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertFalse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TestUtil {

    public static String readResourceAsString(String path) throws RuntimeException {
        return readResourceAsString(path, Charset.defaultCharset().name());
    }

    public static String readResourceAsString(String path, String charsetName) throws RuntimeException {
        try {
            InputStream resource = new ClassPathResource(path).getInputStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, charsetName))) {
                return reader.lines()
                        .collect(Collectors.joining("\n"));
            }
        } catch (Exception exc) {
            throw new RuntimeException("Fout bij uitlezen bestand " + path, exc);
        }
    }

    public static void assertXmlFromFileEquals(String expectedFileName, String actual)
            throws Exception {
        assertXmlEquals(readResourceAsString(expectedFileName), actual);
    }

    public static void assertXmlEquals(String expected, String actual) throws Exception {
        Diff diff = DiffBuilder.compare(expected)
                .ignoreWhitespace()
                .checkForSimilar()
                .withTest(actual)
                .withNodeFilter(node -> !node.getNodeName().equals("version")) //negeer teruggegeven version element
                .build();
        assertFalse(diff.toString() + "\nActual: " + actual, diff.hasDifferences());
    }

    public static void assertJsonFromFileEquals(String expectedFileName, String actual) {
        assertJsonEquals(readResourceAsString(expectedFileName), actual);
    }

    public static void assertJsonEquals(String expected, String actual) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            assertEquals(mapper.readTree(expected),mapper. readTree(actual));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}