package nl.rotterdam.service.geval.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

import nl.rotterdam.service.geval.service.RequestContext;
import nl.rotterdam.service.geval.service.RequestContextService;

// Klopt het dat de requests worden gelogged met alle inhoud van dien?
// Is het ook zo dat dan alle gegevens (BSN/EMAIL) leesbaar worden via
// de logs door Jan en Alleman?
// Dit is overegins een beste impact op performance van de apllicatie
// Gebruik een @Profile("een uit/aan-zet profiel") om dit optioneel te maken
/**
 * Filter voor het bijhouden van een eigen request context en het loggen van
 * ontvangen requests en geretourneerde responses.
 */
@Component
public class RequestLogger extends OncePerRequestFilter {

    @Autowired
    private RequestContextService requestContextService;

    @Autowired
    private Logger logger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        requestContextService.reset();

        final ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        final ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        final RequestContext context = registerRequest(requestWrapper);
        filterChain.doFilter(requestWrapper, responseWrapper);

        registerResponse(responseWrapper);
        responseWrapper.copyBodyToResponse();

        if (logger.isLoggingOn()) {
            logger.logRequest(context);
            logger.logResponse(context);
        }
    }

    private RequestContext registerRequest(final ContentCachingRequestWrapper request) throws IOException {
        final String requestUrl = request.getRequestURL().toString();
        final Map<String, String> headers = new HashMap<>();
        final Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        // dode code
        final String requestBody = new String(request.getContentAsByteArray(), UTF_8);

        return requestContextService.createContext(requestUrl, request.getMethod(), headers, request);
    }

    private void registerResponse(final ContentCachingResponseWrapper response) throws IOException {
        final Map<String, String> headers = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            headers.put(headerName, response.getHeader(headerName));
        }
        final String responseBody = IOUtils.toString(response.getContentInputStream(), UTF_8);
        requestContextService.registerResponse(response.getStatus(), headers, responseBody);
    }
}
