/*
 * Copyright 2022, Gemeente Rotterdam, auteursrecht voorbehouden – BCO, the Netherlands
 */
package nl.rotterdam.service.geval.service.validator.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class EmailConfiguration {

    @Bean
    public LdapContextSource contextSource(
            @Value("${ldap.endpoint}") String url,
            @Value("${ldap.userDn}") String userDn,
            @Value("${ldap.password}") String password) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setUserDn(userDn);
        contextSource.setPassword(password);
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        final LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate;
    }

    @Bean
    @Profile("!mock")
    public EmailAccountLookup emailAccountLookup() {
        return new EmailAccountLookupImpl();
    }
}
