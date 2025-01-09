package nl.rotterdam.service.geval.service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import nl.rotterdam.service.geval.api.v1.json.GevalAntwoord;
import nl.rotterdam.service.geval.api.v1.json.GevalVraag;
import nl.rotterdam.service.geval.foutafhandeling.exceptions.InvalideInputException;
import nl.rotterdam.service.geval.foutafhandeling.exceptions.TimeoutException;
import nl.rotterdam.service.geval.service.validator.Validators;
import nl.rotterdam.service.geval.transform.Json2XmlModelTransformer;
import nl.rotterdam.service.geval.transform.Xml2JsonModelTransformer;
import nl.rotterdam.service.geval.util.soap.DefaultNamespaces;
import nl.rotterdam.service.geval.util.soap.SoapUtil;
import nl.rotterdam.service.geval.util.soap.XsdValidator;
import nl.rotterdam.service.geval.ws.ExceptionHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.filter.MethodInvokingSelector;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.transformer.MethodInvokingTransformer;
import org.springframework.integration.xml.transformer.MarshallingTransformer;
import org.springframework.integration.xml.transformer.ResultToStringTransformer;
import org.springframework.integration.xml.transformer.UnmarshallingTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.ws.config.annotation.EnableWs;
import org.xml.sax.SAXException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@EnableWs
@Configuration
public class ValidatieIntegrationFlow extends AbstractIntegrationFlow {
    /**
     * Deze constante is bedoeld om een kleine marge aan te houden voordat er een definitieve time-out
     * gaat plaatsvinden. Validators kunnen binnen deze marge er nog voor opteren om ingekorte validaties
     * uit te voeren; zie bijvoorbeeld status 'TIJDGEBREK' van EmailValidator.
     */
    private final static int TIMEOUT_MARGE = 20;

    @Autowired
    private RequestContextService requestContextService;

    @Autowired
    private ExceptionHandler exceptionHandler;

    @Autowired
    private Marshaller marshaller;

    @Autowired
    private Unmarshaller unmarshaller;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientHttpRequestFactory requestFactory;

    @Autowired
    private Validators validators;

    @Value("${application.version}")
    private String applicationVersion;

    @Value("${geval.max.checks:10}")
    private int maxChecks = 10;

    @Value("${geval.timeoutMs:3000}")
    private int timeoutMs;

    @Autowired
    private ExecutorService executorService;

    @Bean
    public IntegrationFlow validatieFlow() {
        return IntegrationFlows.from(gevalGateway())
            .enrichHeaders(log("GEVAL.ROUTE"))
            .route(Message.class,
                    m -> {
                            final MediaType contentType = m.getHeaders().get(MessageHeaders.CONTENT_TYPE, MediaType.class);
                            if (contentType == null) {
                                throw new InvalideInputException("Ontbrekende Content-Type header");
                            } else if (contentType.toString().startsWith("text/xml")) {
                                final String soapAction = getCaseInsensitiveHeader(m.getHeaders(), "SOAPAction");
                                if (soapAction == null) {
                                    throw new InvalideInputException("Ontbrekende SOAPAction header");
                                }
                                switch (soapAction) {
                                    case "geval":
                                    case "\"geval\"":
                                        return "xml";
                                    default:
                                        throw new InvalideInputException("Onbekende SOAPAction in headers: " + soapAction);
                                }
                            } else if (contentType.toString().startsWith("application/json")) {
                                return "json";
                            } else {
                                throw new InvalideInputException("Ongeldige Content-Type: " + contentType);
                            }
                        },
                    m -> m.channelMapping("xml", "channelXML")
                          .channelMapping("json", "channelJSON")
            )
            .get();
    }

    @Bean
    public IntegrationFlow gevalXmlFlow() throws SAXException {
        return IntegrationFlows.from("channelXML")
            .enrichHeaders(log("GEVAL.SOAP.UNWRAP", null))
            .transform(unwrapSoapTransformer())
            .enrichHeaders(log("GEVAL.VALIDATE.XML", null))
            .filter(gevalXmlValidator())
            .enrichHeaders(log("GEVAL.UNMARSHALL.XML"))
            .transform(unmarshallingTransformer())
            .enrichHeaders(log("GEVAL.XML2JSON"))
            .transform(this::vraagXml2Json)
            .enrichHeaders(log("GEVAL.EXECUTE"))
            .transform(this::validate)
            .enrichHeaders(log("GEVAL.JSON2XML"))
            .transform(this::antwoordJson2Xml)
            .enrichHeaders(log("GEVAL.MARSHALL.XML"))
            .transform(marshallingTransformer())
            .enrichHeaders(log("GEVAL.SOAP.WRAP"))
            .transform(wrapSoapTransformer())
            .enrichHeaders(h -> h.header("Content-Type", "text/xml", true))
            .channel(replyChannel())
            .get();
    }

    @Bean
    public IntegrationFlow gevalJsonFlow() {
        return IntegrationFlows.from("channelJSON")
            .enrichHeaders(log("GEVAL.UNMARSHALL.JSON", null))
            .transform(this::unmarshallJsonVraag)
            .enrichHeaders(log("GEVAL.EXECUTE"))
            .transform(this::validate)
            .enrichHeaders(log("GEVAL.MARSHALL.JSON"))
            .transform(this::marshallJsonAntwoord)
            .enrichHeaders(h -> h.header("Content-Type", "application/json", true))
            .channel(replyChannel())
            .get();
    }

    /**
     * Valideer de lijst van te valideren gegevens met inachtneming van de ingestelde timeout waarde.
     * Het controleren op timeouts is op twee niveaus ingeregeld:<UL>
     *     <LI>het bovenste niveau controleert de overall tijd,
     *         en zal in geval van een timeout tot een technische fout leiden</LI>
     *     <LI>het onderste niveau controleert de tijd per check,
     *         en zal in geval van een tekort aan tijd tot mogelijk ingekorte checks leiden</LI>
     * </UL>
     * @param vraag
     * @return GevalAntwoord
     * @throws TimeoutException
     */
    private GevalAntwoord validate(final GevalVraag vraag) throws TimeoutException {
        if (vraag.getChecks().isEmpty()) {
            throw new InvalideInputException("Ontbrekende checks");
        }
        if (vraag.getChecks().size() > maxChecks) {
            throw new InvalideInputException(String.format("Aantal checks (%d) mag niet hoger zijn dan %d", vraag.getChecks().size(), maxChecks));
        }
        logger.log("geval.aantal.checks=" + vraag.getChecks().size());

        Future<GevalAntwoord> future = executorService.submit(() -> {
            final var start = System.currentTimeMillis();
            final var antwoord = new GevalAntwoord();
            vraag.getChecks().forEach(check -> {
                // Houdt bij hoeveel tijd we nog over hebben
                final var tijdsbestek = timeoutMs - (int)(System.currentTimeMillis() - start);
                antwoord.addChecksItem(validators.valideer(check, tijdsbestek));
            });
            return antwoord;
        });
        try {
            // Overall timeout met een kleine marge extra
            return future.get(timeoutMs + TIMEOUT_MARGE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException|ExecutionException ex) {
            throw new IllegalStateException(ex);
        } catch (java.util.concurrent.TimeoutException ex) {
            throw new TimeoutException("Validatie is afgebroken vanwege timeout", ex);
        } finally {
            future.cancel(true);
        }
    }

    private RequestMapping createMapping(final HttpMethod[] method, final String... path) {
        RequestMapping requestMapping = new RequestMapping();
        requestMapping.setMethods(method);
        requestMapping.setPathPatterns(path);

        return requestMapping;
    }

    @Bean
    GenericTransformer<String,String> unwrapSoapTransformer() {
        return payload -> SoapUtil.toString(SoapUtil.removeSoapEnvelope(SoapUtil.parse(payload)));
    }

    @Bean
    GenericTransformer<String,String> wrapSoapTransformer() {
        return payload -> SoapUtil.toString(SoapUtil.addSoapEnvelope(SoapUtil.parse(payload), DefaultNamespaces.SOAPENVELOPE11));
    }

    @Bean
    DirectChannel inputChannel() {
        return new DirectChannel();
    }
    @Bean
    DirectChannel replyChannel() {
        return new DirectChannel();
    }
    @Bean
    DirectChannel errorChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlows.from(errorChannel())
                .transform(new MethodInvokingTransformer(exceptionHandler, "handleException"))
                .get();
    }

    @Bean
    public MethodInvokingSelector gevalXmlValidator() throws SAXException {
        return new MethodInvokingSelector(new XsdValidator("xsd/geval-operations-v1.xsd",
                "xsd/soap_1_1.xsd"), "validate");
    }

    @Bean
    public HeaderMapper<HttpHeaders> headerMapper() {
        final var mapper = new DefaultHttpHeaderMapper();
        mapper.setInboundHeaderNames("Content-Type", "SOAPAction");
        mapper.setOutboundHeaderNames("Content-Type", "SOAPAction");
        return mapper;
    }

    @Bean
    public HttpRequestHandlingMessagingGateway gevalGateway() {
        final var handler = new HttpRequestHandlingMessagingGateway();
        handler.setRequestMapping(createMapping(new HttpMethod[] { HttpMethod.POST }, "/"));
        handler.setHeaderMapper(headerMapper());
        handler.setRequestChannel(inputChannel());
        handler.setReplyChannel(replyChannel());
        handler.setErrorChannel(errorChannel());
        return handler;
    }

    @Bean
    UnmarshallingTransformer unmarshallingTransformer() {
        return new UnmarshallingTransformer(unmarshaller);
    }

    @Bean
    MarshallingTransformer marshallingTransformer() {
        return new MarshallingTransformer(marshaller, new ResultToStringTransformer());
    }

    private GevalVraag unmarshallJsonVraag(final Object payload) {
        try {
            if (payload instanceof String) {
                return objectMapper.readValue((String) payload, GevalVraag.class);
            } else {
                return objectMapper.readValue((byte[])payload, GevalVraag.class);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String marshallJsonAntwoord(final GevalAntwoord antwoord) {
        try {
            return objectMapper.writeValueAsString(antwoord);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private GevalVraag vraagXml2Json(final nl.rotterdam.service.geval.api.v1.xml.GevalVraag xmlVraag) {
        return new Xml2JsonModelTransformer().transform(xmlVraag);
    }

    private nl.rotterdam.service.geval.api.v1.xml.GevalAntwoord antwoordJson2Xml(final GevalAntwoord jsonAntwoord) {
        return new Json2XmlModelTransformer().transform(jsonAntwoord);
    }

    /**
     * Retourneer de waarde van de header waarvan de naam ongeacht case overeenkomt met parameter <code>headerName</code>.
     * Dit is met name van belang voor HTTP headers. Deze zijn case-insensitive.
     *
     * @param headers
     * @param headerName
     * @return
     */
    private String getCaseInsensitiveHeader(final MessageHeaders headers, final String headerName) {
        final LinkedCaseInsensitiveMap map = new LinkedCaseInsensitiveMap();
        map.putAll(headers);
        return (String) map.get(headerName);
    }
}
