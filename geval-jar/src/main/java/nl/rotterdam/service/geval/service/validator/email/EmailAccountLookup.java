package nl.rotterdam.service.geval.service.validator.email;

import java.util.Optional;

/**
 * Lookup van e-mail account in Rotterdam
 */
public interface EmailAccountLookup {
    Optional<EmailAccount> findByEmailAdres(String emailAdres);
}
