package nl.rotterdam.service.geval.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nl.rotterdam.service.geval.AbstractTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@SpringIntegrationTest
@DirtiesContext
public class ExceptionHandlerTest extends AbstractTest {

    @Autowired
    @Qualifier("errorChannel")
    private DirectChannel testInputChannel;

    @Autowired
    @Qualifier("errorHandlingFlow")
    private IntegrationFlow flowToBeTested;

}