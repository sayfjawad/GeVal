package nl.rotterdam.service.geval.service.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import nl.rotterdam.service.geval.api.v1.json.Check;
import nl.rotterdam.service.geval.api.v1.json.CheckResultaat;
import nl.rotterdam.service.geval.api.v1.json.ValidatieType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Samenstel van specifieke validators voor de generieke uitvoering van validaties
 */
@Component
public class Validators {

    private List<Validator> validatorList;
    private final Map<ValidatieType,Validator> validatorMap = new HashMap<>();

    /**
     * Valideer gegeven met inachtneming van tijdsbestek
     *
     * @param check        uit te voeren validatie
     * @param tijdsbestek  indien negatief of gelijk aan nul, dient er zo mogelijk een ingekorte
     *                     check te worden uitgevoerd; waarde in milliseconden
     * @return CheckResultaat
     */
    public CheckResultaat valideer(final Check check, final int tijdsbestek) {
        return validatorMap.get(check.getType()).valideer(check.getGegeven(), tijdsbestek);
    }

    // Als we toch een List gaan auto-wiren met validators dan kunnen we net zo goed dezelfde List
    // op een functional wijze gebruiken. Dan heb je deze hele class niet nodig
    /**
     * validators.stream()
     *           .filter(validator -> validator.getGegevenstype().equals(gegeven))
     *           .enZoVoort();
     */
    @Autowired
    public void setValidators(List<Validator> validators) {
        this.validatorList = validators;
    }

    @PostConstruct
    private void initialize() {
        validatorList.forEach(validator -> validatorMap.put(validator.getGegevenstype(), validator));
    }
}
