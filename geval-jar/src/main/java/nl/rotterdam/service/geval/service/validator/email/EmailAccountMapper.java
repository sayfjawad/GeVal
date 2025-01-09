package nl.rotterdam.service.geval.service.validator.email;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;

public class EmailAccountMapper implements AttributesMapper<EmailAccount> {

    private static final Logger log = LoggerFactory.getLogger(EmailAccountMapper.class);
    public static final String LDAP_ATTRIBUTE_EMAIL_ADRES = "mail";
    public static final String LDAP_ATTRIBUTE_DISTINGUISHED_NAME = "distinguishedName";

    @Override
    public EmailAccount mapFromAttributes(final Attributes attributes) throws NamingException {

        return new EmailAccount()
                .emailAdres(getAttribute(attributes, LDAP_ATTRIBUTE_EMAIL_ADRES));
    }

    private <T> T getAttribute(final Attributes attributes, final String attributeName)
            throws NamingException {

        final var attribute = attributes.get(attributeName);
        if (attribute == null) {
            log.warn("LDAP attribuut {} is niet gezet voor {}", attributeName,
                    attributes.get(LDAP_ATTRIBUTE_DISTINGUISHED_NAME));
            return null;
        }
        return (T) attribute.get();
    }
}
