package nl.rotterdam.service.geval;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;

@PropertySource(value = { "classpath:version.properties" })
public abstract class AbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);

    static {
        System.setProperty("spring.main.allow-bean-definition-overriding", "true");
    }

    @BeforeEach
    public void setup() {
        BrokerCreator.createForTesting();
        doBeforeEach();
    }

    protected void doBeforeEach() { }

    protected String replaceString(String bron, String start, String end, String replace) {
        int indexStart = bron.indexOf(start);
        if (indexStart == -1) {
            return bron;
        }
        indexStart += start.length();
        int indexEnd = bron.indexOf(end, indexStart);
        if (indexEnd == -1) {
            return bron;
        }
        return bron.substring(0, indexStart) + replace + bron.substring(indexEnd);
    }


    protected static void assertContains(String contains, String jmsText) {
        // "Tekst " + contains + " niet gevonden in " + jmsText,
        assertTrue(jmsText.contains(contains));
    }

    protected static String getElementText(String jmsText, String elementName) {
        int startIndex = jmsText.indexOf("<" + elementName + ">");
        if (startIndex == -1) {
            return null;
        }
        int endIndex = jmsText.indexOf("</" + elementName + ">", startIndex);
        if (endIndex == -1) {
            return null;
        }
        return jmsText.substring(startIndex + 2 + elementName.length(), endIndex);
    }

    protected String getResource(String filename) throws Exception {
        try (InputStream inputStream = new ClassPathResource(filename).getInputStream()) {
            if (inputStream != null) {
                return IOUtils.toString(inputStream, Charset.defaultCharset());
            }
        }
        throw new FileNotFoundException("Bestand " + filename + " niet gevonden");
    }
}
