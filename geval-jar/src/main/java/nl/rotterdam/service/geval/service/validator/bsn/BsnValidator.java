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
 * Indien het nummer per cijfer wordt aangeduid (s0 s1 s2 s3 s4 s5 s6 s7 s8), dan is de volgende voorwaarde te controleren:<br/>
 * <pre>(9*s0)+(8*s1)+(7*s2)+...+(2*s7)-(1*s8) is deelbaar door 11</pre>
 */
@Component
@Profile("!mock")
public class BsnValidator implements Validator {

    /**

     ## How the eleven proof works for BSN

     This example uses the Dutch implementation of the eleven proof for the social security number
     equivalent 'BSN - Burger Service Nummer'.

     BSN number | 2 | 5 | 3 | 0 | 4 | 7 | 1 | 4 | 6 |

     |             | #1 | #2 | #3 | #4 | #5 | #6 | #7 | #8 | #9 |       |     |        |   |
     |-------------|----|----|----|----|----|----|----|----|----|-------|-----|--------|---|
     | Number      | 2  | 5  | 3  | 0  | 4  | 7  | 1  | 4  | 6  |       |     |        |   |
     |             | x  | x  | x  | x  | x  | x  | x  | x  | x  |       |     |        |   |
     | Multipliers | 9  | 8  | 7  | 6  | 5  | 4  | 3  | 2  | -1 |       |     |        |   |
     | result      | 18 | 40 | 21 | 0  | 20 | 28 | 3  | 8  | -6 | Total | 132 | % 11 = | 0 |

     */


    @Override
    public ValidatieType getGegevenstype() {

        return ValidatieType.BSN;
    }

    @Override
    public CheckResultaat valideer(String waarde, int tijdsbestek) {

        boolean isBSN = false;
//          if (waarde.matches("\\d?\\d{8}")) {
        if (waarde.matches("[0-9]?[0-9]{8}")) {
            final long value = Long.valueOf(waarde);
            //final long value = Long.parseLong(waarde);
            if (value > 10_000_000) {
                int acc = 0;
                final String bsn = String.format("%09d", value);
                // Is dit leesbaar?
                for (int i = 0, factor = 9; i < 8; i++, factor--) {
                    acc += factor * (bsn.charAt(i) - '0');
                }
                // Kan de volgende developer hieruit maken waarom dit gebeurt?
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
