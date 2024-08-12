package nl.rotterdam.service.geval.service;

import java.util.Map;

/**
 * Klasse met informatie over de context van een request ter ondersteuning
 * van de verwerking van het request of voor de logging, en diagnosticeren
 * bij fouten.
 */
public interface RequestContext {

    /**
     * @return unieke identificatie van de request
     */
    String getId();

    /**
     * @return identificatie van het aanvragende proces
     */
    String getProcescode();

    String getRequestUrl();

    String getRequestMethod();

    Map<String, String> getRequestHeaders();

    /**
     * Retourneert de inhoud van het requestbericht.
     *
     * @return berichtinhoud van request
     */
    String getRequestBody();

    int getResponseStatus();

    /**
     * Retourneert de inhoud van het responsebericht
     *
     * @return berichtinhoud van response of <code>null</code> indien nog geen response
     */
    String getResponseBody();

    Map<String, String> getResponseHeaders();

    /**
     * @param procescode
     */
    void setProcescode(String procescode);
}
