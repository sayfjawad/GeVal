package nl.rotterdam.service.geval.transform;

import nl.rotterdam.service.geval.AbstractTest;
import nl.rotterdam.service.geval.TestUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.integration.transformer.GenericTransformer;

@SpringBootTest
@SpringIntegrationTest
public class SoapEnvelopeTest extends AbstractTest {
    @Autowired
    private GenericTransformer<String,String> unwrapSoapTransformer;

    @Test
    void soapBodyMatchTest() throws Exception {
        String message = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "<soap:Body>\n" +
                "<FIN:Di01-verstrekVordering xmlns:StUF=\"http://www.egem.nl/StUF/StUF0301\" xmlns:BG=\"http://www.egem.nl/StUF/sector/bg/0310\" xmlns:FIN=\"http://www.egem.nl/StUF/sector/fin/0310\">\n" +
                "   <FIN:stuurgegevens/>\n" +
                "</FIN:Di01-verstrekVordering>\n" +
                "</soap:Body>\n" +
                "</soap:Envelope>";
        String transformed = unwrapSoapTransformer.transform(message);
        TestUtil.assertXmlEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><FIN:Di01-verstrekVordering xmlns:FIN=\"http://www.egem.nl/StUF/sector/fin/0310\">\n" +
                "   <FIN:stuurgegevens/>\n" +
                "</FIN:Di01-verstrekVordering>", transformed);
    }

    @Test
    void soapBodyNoMatchTest() throws Exception {
        String message = "<FIN:Di01-verstrekVordering xmlns:StUF=\"http://www.egem.nl/StUF/StUF0301\" xmlns:BG=\"http://www.egem.nl/StUF/sector/bg/0310\" xmlns:FIN=\"http://www.egem.nl/StUF/sector/fin/0310\">\n" +
                        "   <FIN:stuurgegevens/>\n" +
                        "</FIN:Di01-verstrekVordering>";
        String transformed = unwrapSoapTransformer.transform(message);
        TestUtil.assertXmlEquals(message, transformed);
    }
}
