package nl.rotterdam.service.geval.service.validator.email;

/**
 * E-mail account gegevens
 */
public class EmailAccount {
    private String emailAdres;

    public String getEmailAdres() {
        return this.emailAdres;
    }

    // waarom hier geen constructor gebruiken?
    public EmailAccount emailAdres(final String emailAdres) {
        this.emailAdres = emailAdres;
        return this;
    }
}
