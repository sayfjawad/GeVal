package nl.rotterdam.service.geval.ws;

import java.util.HashMap;
import java.util.Map;

import nl.rotterdam.service.geval.service.RequestContext;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Logger voor requests en responses
 */
@Component
public class Logger {

    private final static int DEFAULT_PAYLOAD_LIMIT = 1000;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RequestLogger.class);

    @Value("${geval.log.requests}")
    private boolean loggingOn = true;

    @Value("${geval.log.payload-limit: 1000}")
    private int payloadLimit = DEFAULT_PAYLOAD_LIMIT;

    public boolean isLoggingOn() {
        return this.loggingOn || LOGGER.isDebugEnabled();
    }

    public void logRequest(final RequestContext context) {
        LOGGER.info("======================================request===================================================");
        LOGGER.info("URI         : {}", context.getRequestUrl());
        LOGGER.info("Method      : {}", context.getRequestMethod());
        LOGGER.info("Headers     : {}", clean(context.getRequestHeaders()));
        LOGGER.info("Request body: {}", clip(context.getRequestBody()));
    }

    public void logResponse(final RequestContext context) {
        final HttpStatus httpStatus = HttpStatus.valueOf(context.getResponseStatus());
        LOGGER.info("======================================response==================================================");
        LOGGER.info("Status code  : {}", httpStatus.value());
        LOGGER.info("Status text  : {}", httpStatus.getReasonPhrase());
        LOGGER.info("Headers      : {}", clean(context.getResponseHeaders()));
        LOGGER.info("Response body: {}", clip(context.getResponseBody()));
    }

    public void log(final String procesStap) {
        log(procesStap, (String)null);
    }

    public void log(final String procesStap, final String message) {
        if (isLoggingOn()) {
            LOGGER.info(procesStap);
            if (message != null) {
                LOGGER.info(message);
            }
        }
    }

    public void log(final String procesStap, final Message<?> message) {
        log(procesStap, message.getPayload().toString());
    }

    /**
     * Maximeer het string argument qua lengte voor logging doeleinden.
     * @param text
     * @return
     */
    public String clip(final String text) {
        return text.length() <= getPayloadLimit() ? text : text.substring(0, getPayloadLimit()) + "...";
    }

    private Map<String,String> clean(final Map<String,String> headers) {
        Map<String, String> cleaned = new HashMap<>();
        headers.forEach((k, v) -> cleaned.put(k, "Authorization".equalsIgnoreCase(k) ? "...." : v));
        return cleaned;
    }

    public int getPayloadLimit() {
        return this.payloadLimit;
    }

}
