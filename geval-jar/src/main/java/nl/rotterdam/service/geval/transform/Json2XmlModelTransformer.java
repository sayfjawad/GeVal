package nl.rotterdam.service.geval.transform;

import nl.rotterdam.service.geval.api.v1.json.Validatie;
import nl.rotterdam.service.geval.api.v1.json.ValidatieType;
import nl.rotterdam.service.geval.api.v1.xml.GegevensType;
import nl.rotterdam.service.geval.api.v1.xml.GevalAntwoord;
import nl.rotterdam.service.geval.api.v1.xml.GevalVraag;
import nl.rotterdam.service.geval.api.v1.xml.ResultaatType;
import nl.rotterdam.service.geval.api.v1.xml.ValidatieCheckResultType;
import nl.rotterdam.service.geval.api.v1.xml.ValidatieCheckType;

/**
 * Transformer voor JSON-gebaseerd model naar XML-gebaseerd model.
 */
public class Json2XmlModelTransformer {

    public GevalVraag transform(final nl.rotterdam.service.geval.api.v1.json.GevalVraag jsonVraag) {

        final var xmlVraag = new GevalVraag();
        xmlVraag.setProcescode(jsonVraag.getProcescode());
        jsonVraag.getChecks().forEach(jsonCheck -> {
            final ValidatieCheckType xmlCheck = new ValidatieCheckType();
            xmlCheck.setType(gegevenstype(jsonCheck.getType()));
            xmlCheck.setGegeven(jsonCheck.getGegeven());
            xmlVraag.getCheck().add(xmlCheck);
        });
        return xmlVraag;
    }

    public GevalAntwoord transform(
            final nl.rotterdam.service.geval.api.v1.json.GevalAntwoord jsonAntwoord) {

        final var xmlAntwoord = new GevalAntwoord();
        jsonAntwoord.getChecks().forEach(jsonCheckresult -> {
            final ValidatieCheckResultType xmlCheckResult = new ValidatieCheckResultType();
            xmlCheckResult.setType(gegevenstype(jsonCheckresult.getType()));
            xmlCheckResult.setGegeven(jsonCheckresult.getGegeven());
            xmlCheckResult.setValidatie(resultaat(jsonCheckresult.getValidatie()));
            jsonCheckresult.getDetails()
                    .forEach(detail -> xmlCheckResult.getDetail().add(detail.toString()));
            xmlCheckResult.setToelichting(jsonCheckresult.getToelichting());
            xmlAntwoord.getCheckResult().add(xmlCheckResult);
        });
        return xmlAntwoord;
    }

    private ResultaatType resultaat(Validatie resultaat) {

        switch (resultaat) {
            case GOED:
                return ResultaatType.GOED;
            case FOUT:
                return ResultaatType.FOUT;
            case AMBIVALENT:
                return ResultaatType.AMBIVALENT;
            default:
                throw new IllegalArgumentException();
        }
    }

    private GegevensType gegevenstype(ValidatieType type) {

        switch (type) {
            case E_MAIL:
                return GegevensType.E_MAIL;
            case BSN:
                return GegevensType.BSN;
            default:
                throw new IllegalArgumentException();
        }
    }
}
