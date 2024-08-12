package nl.rotterdam.service.geval.ws.controller;

import java.util.ArrayList;
import java.util.List;

import nl.rotterdam.service.geval.api.v1.json.CheckResultaat;
import nl.rotterdam.service.geval.service.validator.bsn.BsnValidator;
import nl.rotterdam.service.geval.service.validator.email.EmailAccount;
import nl.rotterdam.service.geval.service.validator.email.EmailAccountLookup;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("mock")
public class MockTestConfiguration {
    @Bean
    public EmailAccountLookup emailAccountLookup() {
        return emailAdres -> {
            final List<EmailAccount> bekend = new ArrayList<>();
            bekend.add(new EmailAccount().emailAdres("w.hu1@rotterdam.nl"));
            bekend.add(new EmailAccount().emailAdres("g.huizer@rotterdam.nl"));
            return bekend.stream().filter(ea -> ea.getEmailAdres().equals(emailAdres)).findFirst();
        };
    }

    @Bean
    public BsnValidator bsnValidator() {
        return new BsnValidator() {
            @Override
            public CheckResultaat valideer(String waarde, int tijdsbestek) {
                // Ten behoeve van klasse TimeoutSIT neemt een check 1,5 seconde in beslag.
                // De time-out instelling is iets meer dan 1,5 seconde. Er blijft dus nog
                // maar heel weinig tijd over om binnen het tijdslimiet te blijven.
                try {
                    Thread.sleep(1500L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return super.valideer(waarde, tijdsbestek);
            }
        };
    }
}
