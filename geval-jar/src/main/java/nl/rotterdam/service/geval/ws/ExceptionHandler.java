package nl.rotterdam.service.geval.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.dom.DOMResult;
import nl.rotterdam.service.geval.api.v1.json.Fout;
import nl.rotterdam.service.geval.foutafhandeling.Foutcodes;
import nl.rotterdam.service.geval.foutafhandeling.exceptions.InvalideInputException;
import nl.rotterdam.service.geval.foutafhandeling.exceptions.TimeoutException;
import nl.rotterdam.service.geval.util.soap.SoapUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

/**
 * Exception handler voor fouten in Spring Integration flows.
 */
@Component
public class ExceptionHandler {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);
    @Autowired
    private ObjectMapper objectMapper;

    public Message<String> handleException(final ErrorMessage errorMessage) throws IOException {

        MessagingException messagingException = (MessagingException) errorMessage.getPayload();
        Message<?> failedMessage = messagingException.getFailedMessage();
        while (messagingException.getCause() instanceof MessagingException) {
            messagingException = (MessagingException) messagingException.getCause();
            if (messagingException.getFailedMessage() != null) {
                failedMessage = messagingException.getFailedMessage();
            }
        }
        final String procesStap = getProcesStap(failedMessage);
        final Throwable cause = messagingException.getCause();
        LOGGER.debug("Exceptie in ErrorMessage ontvangen", cause);
        final Foutcodes foutcode = foutCode(cause);
        String payload;
        MediaType contentType;
        if (isJsonContent(errorMessage.getOriginalMessage())) {
            payload = objectMapper.writeValueAsString(new Fout()
                    .code(foutcode.toString())
                    .status(foutcode.getHttpStatus())
                    .detail(foutMessage(cause)));
            contentType = MediaType.APPLICATION_PROBLEM_JSON;
        } else {
            final DOMResult result = new DOMResult();
            payload = SoapUtil.toString(SoapUtil.generateSoapFault((Document) result.getNode(),
                    Foutcodes.INVALIDE_INPUT_FOUT.equals(foutcode) ? SoapUtil.SoapFaultCodes.CLIENT
                            : SoapUtil.SoapFaultCodes.SERVER,
                    foutMessage(cause)));
            contentType = MediaType.TEXT_XML;
        }
        return MessageBuilder.withPayload(payload)
                .copyHeaders(failedMessage.getHeaders())
                .setHeader("http_statusCode", foutcode.getHttpStatus() + "")
                .setHeader("Content-Type", contentType)
                .build();
    }

    private String getOriginalMessage(final ErrorMessage errorMessage) {

        final var payload = errorMessage.getOriginalMessage().getPayload();
        if (payload instanceof Document) {
            return SoapUtil.toString((Document) payload);
        } else if (payload instanceof byte[]) {
            return new String((byte[]) payload, StandardCharsets.UTF_8);
        } else {
            return payload.toString();
        }
    }

    private Foutcodes foutCode(final Throwable exceptie) {

        if (exceptie instanceof SAXParseException) {
            return Foutcodes.INVALIDE_INPUT_FOUT;
        } else if (exceptie instanceof IllegalArgumentException) {
            return Foutcodes.INVALIDE_INPUT_FOUT;
        } else if (exceptie instanceof InvalideInputException) {
            return Foutcodes.INVALIDE_INPUT_FOUT;
        } else if (exceptie instanceof TimeoutException) {
            return Foutcodes.TIMEOUT_FOUT;
        } else {
            return Foutcodes.INTERNE_FOUT;
        }
    }

    private String foutMessage(Throwable fout) {

        String message = fout.getMessage();
        while (fout.getCause() != null) {
            fout = fout.getCause();
            if (fout.getMessage() != null) {
                message = fout.getMessage();
            }
        }
        return message != null ? message : "";
    }

    private String getProcesStap(Message<?> failedMessage) {

        String procesStap = null;
        if (failedMessage != null) {
            procesStap = (String) failedMessage.getHeaders().get("procesStap");
        }
        // Laat postfixes als ":TODO" of ":DONE" weg
        if (procesStap != null && procesStap.contains(":")) {
            procesStap = procesStap.substring(0, procesStap.indexOf(":"));
        }
        return procesStap == null ? "PROCES-STAP" : procesStap;
    }

    private boolean isJsonContent(Message<?> message) {

        final MediaType contentType = (MediaType) message.getHeaders()
                .get(MessageHeaders.CONTENT_TYPE);
        return contentType != null && contentType.toString().startsWith("application/json");
    }
}
