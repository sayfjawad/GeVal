package nl.rotterdam.service.geval.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Implementatie van  request context
 */
public class RequestContextImpl implements RequestContext {
    private final String e2eId;
    private String procescode = "???";
    private final String requestUrl;
    private final Map<String, String> requestHeaders;
    private final String requestMethod;

    /**
     * We onthouden de wrapper zodat we via de cache te weten komen wat de inhoud was van de uitgelezen body
     */
    private final ContentCachingRequestWrapper requestWrapper;
    private int responseStatus;
    private Map<String, String> responseHeaders;
    private String responseBody;

    public RequestContextImpl(final String url, final String method, final Map<String,String> headers, final ContentCachingRequestWrapper request) {
        this.requestWrapper = request;
        this.requestUrl = url;
        this.requestMethod = method;
        this.requestHeaders = headers;
        this.e2eId = UUID.randomUUID().toString();
    }

    @Override
    public String getId() {
        return this.e2eId;
    }

    @Override
    public String getProcescode() {
        return this.procescode;
    }

    @Override
    public String getRequestUrl() {
        return this.requestUrl;
    }

    @Override
    public String getRequestMethod() {
        return this.requestMethod;
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return this.requestHeaders;
    }

    @Override
    public String getRequestBody() {
        return new String(this.requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    public int getResponseStatus() {
        return this.responseStatus;
    }

    @Override
    public String getResponseBody() {
        return this.responseBody;
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }

    @Override
    public void setProcescode(String procescode) {
        assert procescode != null;
        this.procescode = procescode;
    }

    public void setResponseStatus(int status) {
        this.responseStatus = status;
    }

    public void setResponseHeaders(Map<String, String> headers) {
        this.responseHeaders = headers;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}
