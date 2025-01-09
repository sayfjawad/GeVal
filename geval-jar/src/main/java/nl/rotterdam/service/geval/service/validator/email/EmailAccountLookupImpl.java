/*
 * Copyright 2022, Gemeente Rotterdam, auteursrecht voorbehouden – BCO, the Netherlands
 *
 */
package nl.rotterdam.service.geval.service.validator.email;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.util.Assert;

public class EmailAccountLookupImpl implements EmailAccountLookup {

    private static final Logger log = LoggerFactory.getLogger(EmailAccountLookupImpl.class);
    public static final String EMAILADRES_PLACEHOLDER = "%emailAdres%";
    @Autowired
    private LdapTemplate ldapTemplate;
    @Value("${ldap.users.baseDn}")
    private String baseDn;
    private String filterTemplate;
    private final EmailAccountMapper emailAccountMapper;

    public EmailAccountLookupImpl() {

        emailAccountMapper = new EmailAccountMapper();
    }

    @Value("${ldap.email.filter}")
    void setFilterTemplate(String filterTemplate) {

        Assert.isTrue(StringUtils.contains(filterTemplate, EMAILADRES_PLACEHOLDER),
                "filter '" + filterTemplate + " moet de placeholder '" + EMAILADRES_PLACEHOLDER
                        + "' bevatten.");
        this.filterTemplate = filterTemplate;
    }

    @Override
    public Optional<EmailAccount> findByEmailAdres(String emailAdres) {

        Assert.hasText(emailAdres, "emailadres moet gevuld zijn");
        final var filter = filterTemplate.replace(EMAILADRES_PLACEHOLDER, escape(emailAdres));
        try {
            final var result = ldapTemplate.search(
                    query().base(baseDn).filter(filter),
                    (AttributesMapper<EmailAccount>) attributes -> emailAccountMapper.mapFromAttributes(
                            attributes));
            if (result.isEmpty()) {
                log.info("EmailAccount met e-mailadres {} is niet gevonden in LDAP.", emailAdres);
                return Optional.empty();
            }
            return Optional.of(result.get(0));
        } catch (NameNotFoundException e) {
            return Optional.empty();
        }
    }

    private String escape(String value) {

        return value.replace("\\", "\\5C")
                .replace("*", "\\2A")
                .replace("(", "\\28")
                .replace(")", "\\29")
                .replace("\000", "\\00");
    }
}
