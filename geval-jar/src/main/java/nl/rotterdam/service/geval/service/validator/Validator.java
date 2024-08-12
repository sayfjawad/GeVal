package nl.rotterdam.service.geval.service.validator;

import nl.rotterdam.service.geval.api.v1.json.CheckResultaat;
import nl.rotterdam.service.geval.api.v1.json.ValidatieType;

/**
 * Generieke interface voor specifieke validaties. Om nodeloze verbositeit tegen te gaan maken
 * we direct gebruik van de JSON-gebaseerde modelklassen in plaats van een gegeneraliseerd model.
 */
public interface Validator {
    /**
     * @return identificatie van het gegevenstype dat deze validator kan valideren
     */
    ValidatieType getGegevenstype();

    default CheckResultaat valideer(final String waarde) {
        return valideer(waarde, Integer.MAX_VALUE);
    }

    /**
     * Valideer gegeven met inachtneming van tijdsbestek
     * @param waarde
     * @param tijdsbestek  indien negatief of gelijk aan nul, dient er zo mogelijk een ingekorte
     *                     check te worden uitgevoerd; waarde in milliseconden
     * @return
     */
    CheckResultaat valideer(final String waarde, final int tijdsbestek);
}
