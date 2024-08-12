package nl.rotterdam.service.geval.service;

/**
 * Klasse voor het thread-local onthouden van de request context van het huidige request.
 */
public final class RequestContextHolder {
    private static final ThreadLocal<RequestContext> requestContext = new ThreadLocal<>();

    static void reset() {
        requestContext.remove();
    }

    static void register(final RequestContext threadLocalRequestContext) {
        requestContext.set(threadLocalRequestContext);
    }

    /**
     * @return RequestContext-instantie van het huidige request of <code>null</code> indien niet gevonden
     */
    public static RequestContext get() {
        return requestContext.get();
    }
}