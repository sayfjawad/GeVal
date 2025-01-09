package nl.rotterdam.service.geval.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Consumer;
import java.util.function.Function;
import nl.rotterdam.service.geval.ws.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.dsl.HeaderEnricherSpec;
import org.springframework.integration.transformer.support.HeaderValueMessageProcessor;
import org.springframework.messaging.Message;

public abstract class AbstractIntegrationFlow {

    @Autowired
    protected Logger logger;
    @Value("${geval.log.flows:false}")
    private boolean logFlows;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Log naam van processtap en voeg deze toe als header. Log tevens de message payload indien
     * zodanig ingeschakeld.
     *
     * @param procesStap
     * @return
     */
    protected Consumer<HeaderEnricherSpec> log(final String procesStap) {

        return log(procesStap, message -> {
            if (logFlows) {
                // Toon inhoud van message als JSON
                try {
                    logger.log(objectMapper.writeValueAsString(message.getPayload()));
                } catch (JsonProcessingException e) {
                    logger.log(message.getPayload().toString());
                }
            }
        });
    }

    /**
     * Log naam van processtap en voeg deze toe als header. Roep bovendien een routine aan om
     * additionele logging te realiseren.
     *
     * @param procesStap
     * @param messageLogger consumer of Message for logging purposes
     * @return
     */
    protected Consumer<HeaderEnricherSpec> log(final String procesStap,
            final Consumer<Message> messageLogger) {

        return h -> h.header("procesStap", new HeaderValueMessageProcessor<String>() {
            @Override
            public String processMessage(Message message) {
                // Haal E2EUUID op uit RequestContext
                final var requestContext = RequestContextHolder.get();
                final var e2euuid = requestContext != null ? requestContext.getId() : null;
                Function<Object, String> stringify = o -> o == null ? null : o.toString();
                final var url = stringify.apply(message.getHeaders().get("http_requestUrl"));
                final var timestamp = stringify.apply(message.getHeaders().get("timestamp"));
                logger.log("** " + url + ";" + procesStap + ";" + timestamp + ";" + e2euuid);
                if (messageLogger != null) {
                    messageLogger.accept(message);
                }
                return procesStap;
            }

            @Override
            public Boolean isOverwrite() {

                return true;
            }
        });
    }
}
