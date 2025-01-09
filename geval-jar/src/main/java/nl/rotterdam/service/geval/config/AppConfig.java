package nl.rotterdam.service.geval.config;

import nl.rotterdam.service.geval.service.validator.email.EmailAccountMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public EmailAccountMapper getEmailAccountMapper() {
        return new EmailAccountMapper();
    }

}
