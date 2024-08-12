package nl.rotterdam.service.geval.service;

import java.util.Map;

import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Verantwoordelijk voor het registreren van de request context.
 */
public interface RequestContextService {
    /**
     * Verwijder de eventueel thread local aanwezige Autorisatie context
     */
    void reset();

    /**
     * Maak een request context aan en bewaar deze thread local.
     * Een eventueel reeds aanwezige request context wordt eerst verwijderd.
     *
     * @param url
     * @param method
     * @param headers
     * @param request wrapper van het originele request waarmee achteraf de nog te ontvangen inhoud is uit te lezen
     * @return nieuwe Autorisatie context
     *
     * @see #reset()
     */
    RequestContext createContext(String url, String method, Map<String, String> headers, ContentCachingRequestWrapper request);

    /**
     * @return thread local Autorisatie context
     * @see #createContext(String, String, Map, ContentCachingRequestWrapper)
     */
    RequestContext getContext();

    void registerResponse(int status, Map<String, String> headers, String responseBody);
}
