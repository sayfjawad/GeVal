package nl.rotterdam.service.geval.service;

import java.util.Map;

import org.springframework.web.util.ContentCachingRequestWrapper;

public class RequestContextServiceImpl implements RequestContextService {
    @Override
    public void reset() {
        RequestContextHolder.reset();
    }

    @Override
    public RequestContext createContext(String url, String method, Map<String,String> headers, final ContentCachingRequestWrapper request) {
        final var context = new RequestContextImpl(url, method, headers, request);
        RequestContextHolder.register(context);
        return context;
    }

    @Override
    public RequestContext getContext() {
        return RequestContextHolder.get();
    }

    @Override
    public void registerResponse(int status, Map<String, String> headers, String responseBody) {
        final var context = (RequestContextImpl) RequestContextHolder.get();
        context.setResponseStatus(status);
        context.setResponseHeaders(headers);
        context.setResponseBody(responseBody);
    }
}
