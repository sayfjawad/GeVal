package nl.rotterdam.service.geval.ws;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.rotterdam.service.geval.service.RequestContext;
import nl.rotterdam.service.geval.service.RequestContextService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Filter voor het bijhouden van een eigen request context en het loggen van ontvangen requests en
 * geretourneerde responses.
 */
@Component
public class RequestLogger extends OncePerRequestFilter {

    @Autowired
    private RequestContextService requestContextService;
    @Autowired
    private Logger logger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        requestContextService.reset();
        final var requestWrapper = new ContentCachingRequestWrapper(request);
        final var responseWrapper = new ContentCachingResponseWrapper(response);
        final var context = registerRequest(requestWrapper);
        filterChain.doFilter(requestWrapper, responseWrapper);
        registerResponse(responseWrapper);
        responseWrapper.copyBodyToResponse();
        if (logger.isLoggingOn()) {
            logger.logRequest(context);
            logger.logResponse(context);
        }
    }

    private RequestContext registerRequest(final ContentCachingRequestWrapper request) {

        final var requestUrl = request.getRequestURL().toString();
        final var headers = new HashMap<String, String>();
        final var headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final var headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return requestContextService.createContext(requestUrl, request.getMethod(), headers,
                request);
    }

    private void registerResponse(final ContentCachingResponseWrapper response) throws IOException {

        final var headers = new HashMap<String, String>();
        for (String headerName : response.getHeaderNames()) {
            headers.put(headerName, response.getHeader(headerName));
        }
        final var responseBody = IOUtils.toString(response.getContentInputStream(), UTF_8);
        requestContextService.registerResponse(response.getStatus(), headers, responseBody);
    }
}
