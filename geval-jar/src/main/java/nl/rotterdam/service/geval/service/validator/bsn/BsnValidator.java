package nl.rotterdam.service.geval.service.validator.bsn;

import nl.rotterdam.service.geval.api.v1.json.CheckResultaat;
import nl.rotterdam.service.geval.api.v1.json.Validatie;
import nl.rotterdam.service.geval.api.v1.json.ValidatieType;
import nl.rotterdam.service.geval.service.validator.Validator;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Klasse ter illustratie van de toevoeging van additionele, specifieke validators
 * <p/>
 * Indien het nummer per cijfer wordt aangeduid (s0 s1 s2 s3 s4 s5 s6 s7 s8),
 * dan is de volgende voorwaarde te controleren:<br/>
 * <pre>(9*s0)+(8*s1)+(7*s2)+...+(2*s7)-(1*s8) is deelbaar door 11</pre>
 */
@Component
@Profile("!mock")
public class BsnValidator implements Validator {
    @Override
    public ValidatieType getGegevenstype() {
        return ValidatieType.BSN;
    }

    @Override
    public CheckResultaat valideer(String waarde, int tijdsbestek) {
        boolean isBSN = false;
        if (waarde.matches("[0-9]?[0-9]{8}")) {
            final long value = Long.valueOf(waarde);
            if (value > 10000000) {
                int acc = 0;
                final String bsn = String.format("%09d", value);
                for (int i=0, factor=9; i < 8; i++, factor--) {
                   acc += factor * (bsn.charAt(i) - '0');
                }
                acc -= bsn.charAt(8) - '0';
                if (acc % 11 == 0) {
                    isBSN = true;
                }
            }
        }
        return new CheckResultaat()
                .type(ValidatieType.BSN)
                .gegeven(waarde)
                .validatie(isBSN ? Validatie.GOED : Validatie.FOUT);
    }
}
