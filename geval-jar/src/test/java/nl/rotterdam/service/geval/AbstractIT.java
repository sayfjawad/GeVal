package nl.rotterdam.service.geval;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.util.ArrayList;
import java.util.List;

import nl.rotterdam.service.geval.service.validator.email.EmailValidator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;

/**
 * Abstracte klasse voor back-end systeemtesten waarbij er gebruik gemaakt wordt van een WireMock stub server.
 */
public class AbstractIT extends AbstractTest {
    private static WireMockServer wireMockServer = null;
    private static final List<RequestInfo> receivedRequests = new ArrayList<>();
    private static final List<ResponseInfo> receivedResponses = new ArrayList<>();

    @Autowired
    private EmailValidator emailValidator;

    @BeforeAll
    static void doBeforeAll() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(8989);
            wireMockServer.start();
            wireMockServer.addMockServiceRequestListener(new RequestListener() {
                @Override
                public void requestReceived(Request request, Response response) {
                    RequestInfo requestInfo = new RequestInfo();
                    receivedRequests.add(requestInfo);
                    requestInfo.url = request.getAbsoluteUrl();
                    requestInfo.body = request.getBodyAsString();
                    requestInfo.headers = request.getHeaders().toString();

                    ResponseInfo responseInfo = new ResponseInfo();
                    receivedResponses.add(responseInfo);
                    responseInfo.body = response.getBodyAsString();
                    responseInfo.headers = response.getHeaders().toString();
                }
            });
            WireMock.configureFor("localhost", wireMockServer.port());
        }
    }

    @BeforeEach
    protected void doBeforeEach() {
        wireMockServer.resetMappings();
        receivedRequests.clear();
        receivedResponses.clear();
    }

    /**
     * Retourneer request ontvangen door stubserver van backend
     *
     * @param offset  een niet-negatieve offset retourneert van eerste naar laatste,
     *                een negatieve offset retourneert van laastste naar eerste
     * @return
     */
    protected RequestInfo getReceivedRequest(final int offset) {
        return receivedRequests.get(offset >= 0 ? offset : receivedRequests.size() + offset);
    }

    /**
     * Retourneer response ontvangen door stubserver van backend
     *
     * @param offset  een niet-negatieve offset retourneert van eerste naar laatste,
     *                een negatieve offset retourneert van laastste naar eerste
     * @return
     */
    protected ResponseInfo getReceivedResponse(final int offset) {
        return receivedResponses.get(offset >= 0 ? offset : receivedResponses.size() + offset);
    }

    protected static class RequestInfo {
        public String url;
        public String body;
        public String headers;
    }

    protected static class ResponseInfo {
        public String body;
        public String headers;
    }

    protected MockHttpServletRequestBuilder postMvc(final String uri) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(uri);
    }

    protected MockHttpServletRequestBuilder getMvc(final String uri) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(uri);
    }

    /**
     * Laat de stub een correcte response retourneren bij bepaalde URL
     * @return
     */
    protected void setupStubResponse(final String url, final String contents, final String response) {
        stubFor(post(urlMatching(url + "(\\?E2EUUID=.+)?"))
                .withRequestBody(containing(contents))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(response)));
    }

    /**
     * Geeft aan of bij de validaties gebruik gemaakt zal worden van LDAP queries
     * @return
     */
    protected boolean isAccountLookupAvailable() {
        return emailValidator.isAccountLookupAvailable();
    }
}
