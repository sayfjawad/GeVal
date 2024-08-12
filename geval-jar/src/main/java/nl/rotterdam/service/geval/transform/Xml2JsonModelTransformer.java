package nl.rotterdam.service.geval.transform;

import nl.rotterdam.service.geval.api.v1.json.Check;
import nl.rotterdam.service.geval.api.v1.json.GevalVraag;
import nl.rotterdam.service.geval.api.v1.json.ValidatieType;
import nl.rotterdam.service.geval.api.v1.xml.GegevensType;

/**
 * Transformer voor XML-gebaseerd model naar JSON-gebaseerd model.
 * Alleen binnenkomende gegevensstructuren worden getransformeerd omdat intern
 * het JSON-gebaseerde model wordt gebruikt voor de verwerking.
 */
public class Xml2JsonModelTransformer {
    public GevalVraag transform(final nl.rotterdam.service.geval.api.v1.xml.GevalVraag xmlVraag) {
        final GevalVraag jsonVraag = new GevalVraag();
        jsonVraag.setProcescode(xmlVraag.getProcescode());
        xmlVraag.getCheck().forEach(xmlCheck -> {
            jsonVraag.addChecksItem(new Check()
                    .type(type(xmlCheck.getType()))
                    .gegeven(xmlCheck.getGegeven()));
        });
        return jsonVraag;
    }

    private ValidatieType type(GegevensType type) {
        switch (type) {
            case E_MAIL:
                return ValidatieType.E_MAIL;
            case BSN:
                return ValidatieType.BSN;
            default:
                throw new IllegalArgumentException();
        }
    }
}
