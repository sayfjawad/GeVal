/*
 * Copyright 2022, Gemeente Rotterdam, Nederland
 * All rights reserved. Without explicit written consent beforehand of the gemeente Rotterdam nothing of this software and source code may be reproduced, adapted, distributed, and/or communicated to the public, except in case of a statutory limitation of copyright.
 */

package nl.rotterdam.service.geval.service.validator.email;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import nl.rotterdam.service.geval.api.v1.json.CheckResultaat;
import nl.rotterdam.service.geval.api.v1.json.Validatie;
import nl.rotterdam.service.geval.api.v1.json.ValidatieType;
import nl.rotterdam.service.geval.service.validator.Validator;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.CommunicationException;
import org.springframework.stereotype.Component;
import org.xbill.DNS.Address;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * Verantwoordelijk voor het valideren van e-mailadressen. Let op! Voor de validatie wordt GEEN
 * gebruik gemaakt van SMTP zelf (o.a. omdat vanwege de netwerkinrichting de SMTP communicatie van
 * deze service zal worden afgewezen door andere SMTP servers)
 */
@Component
public class EmailValidator implements Validator {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EmailValidator.class);
    @Autowired
    private ExecutorService executorService;
    @Value("${geval.my-org.domain}")
    private String myOrgDomain;

    /**
     * Status aanduidingen van een (potentieel) e-mailadres in oplopende mate van
     * kennisvergaring/geavanceerdheid
     */
    private enum EmailStatus {
        /**
         * Invalide adres omdat '@' ontbreekt
         */
        INVALIDE_SYNTAX(Validatie.FOUT),
        /**
         * Gebruiker-deel van adres is invalide
         */
        INVALIDE_SYNTAX_GEBRUIKER(Validatie.FOUT),
        /**
         * Domein-deel van adres is invalide
         */
        INVALIDE_SYNTAX_DOMEIN(Validatie.FOUT),
        /**
         * Domein bestaat niet
         */
        DOMEIN_BESTAAT_NIET(Validatie.FOUT),
        /**
         * Domein heeft geen MX-record
         */
        DOMEIN_ZONDER_EMAIL(Validatie.FOUT),
        /**
         * Domein accepteert e-mails (ook als er geen MX-records zijn)
         */
        DOMEIN_MET_EMAIL(Validatie.GOED),
        /**
         * E-mail account van gebruiker is niet bekend
         */
        ONBEKEND_ACCOUNT(Validatie.FOUT),
        /**
         * E-mailadres bestaat
         */
        GELDIG_ADRES(Validatie.GOED),
        /**
         * Er is een gebrek aan tijd (d.w.z. een time-out is aanstaande) waardoor niet de volledige
         * controle is uitgevoerd. Deze enum is bedoeld als een informatieve, ondersteunende
         * status.
         */
        TIJDGEBREK(null);
        private final Validatie validatie;

        EmailStatus(final Validatie validatie) {

            this.validatie = validatie;
        }

        private Validatie getValidatie() {

            return this.validatie;
        }
    }

    /**
     * Lookup voor e-mailadressen van de eigen organisatie. Het gebruik van de lookup is optioneel
     */
    @Autowired(required = false)
    private EmailAccountLookup emailAccountLookup;
    @Value("${geval.initialization.timeout:5}")
    private int initializationTimeout = 5;
    private final EmailSyntaxValidator syntaxValidator = new EmailSyntaxValidator();

    @Override
    public ValidatieType getGegevenstype() {

        return ValidatieType.E_MAIL;
    }

    @Override
    public CheckResultaat valideer(final String emailAdres, final int tijdsbestek) {

        final var start = System.currentTimeMillis();
        final var status = new ArrayList<EmailStatus>();
        final var domain = getDomainName(emailAdres);
        if (domain == null) {
            status.add(EmailStatus.INVALIDE_SYNTAX);
        } else if (!checkSyntax(emailAdres)) {
            if (!checkDomainSyntax(domain)) {
                status.add(EmailStatus.INVALIDE_SYNTAX_DOMEIN);
            }
            if (!checkGebruikerSyntax(getUser(emailAdres))) {
                status.add(EmailStatus.INVALIDE_SYNTAX_GEBRUIKER);
            }
        } else
            // TODO opzoeken van recente entry in eigen database
            if (!checkDomainExists(domain)) {
                status.add(EmailStatus.DOMEIN_BESTAAT_NIET);
            } else if (!checkDomainHasMX(domain)) {
                status.add(EmailStatus.DOMEIN_ZONDER_EMAIL);
            } else if (!isMyOrg(emailAdres) || emailAccountLookup == null || tijdsbestek <= 0) {
                status.add(EmailStatus.DOMEIN_MET_EMAIL);
                if (tijdsbestek <= 0) {
                    status.add(EmailStatus.TIJDGEBREK);
                }
            } else {
                execute(tijdsbestek - (int) (System.currentTimeMillis() - start),
                        () -> {
                            if (!checkMyOrg(emailAdres)) {
                                status.add(EmailStatus.ONBEKEND_ACCOUNT);
                            } else {
                                status.add(EmailStatus.GELDIG_ADRES);
                            }
                        },
                        () -> {
                            status.add(EmailStatus.DOMEIN_MET_EMAIL);
                            status.add(EmailStatus.TIJDGEBREK);
                        }
                );
            }
        return new CheckResultaat()
                .type(ValidatieType.E_MAIL)
                .gegeven(emailAdres)
                .validatie(status.get(0).getValidatie())
                .details(extractDetails(status));
    }

    /**
     * Voer een check uit met inachtneming van tijdsbestek. Indien de check niet op tijd is
     * uitgevoerd, dient het alternatief te worden uitgevoerd.
     *
     * @param tijdsbestek in milliseconden
     * @param check       uit te voeren check binnen opgegeven tijdsbestek
     * @param alternatief uit te voeren alternatief indien timeout
     */
    private void execute(final int tijdsbestek, final Runnable check, final Runnable alternatief) {

        LOGGER.debug("Tijdsbestek: " + tijdsbestek);
        Future<?> future = executorService.submit(check);
        try {
            future.get(tijdsbestek, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException ex) {
            throw new IllegalStateException(ex);
        } catch (java.util.concurrent.TimeoutException ex) {
            alternatief.run();
        } finally {
            future.cancel(true);
        }
    }

    private List<String> extractDetails(final List<EmailStatus> status) {

        return status.stream().map(Object::toString).collect(Collectors.toList());
    }

    /**
     * Bepaal of de syntax van de domeinnaam van een e-mailadres geldig is
     *
     * @param domainName domeinnaam
     * @return
     */
    boolean checkDomainSyntax(final String domainName) {

        return syntaxValidator.isValidDomain(domainName);
    }

    /**
     * Bepaal of de syntax van de gebruikersnaam van een e-mailadres geldig is
     *
     * @param gebruikersnaam
     * @return
     */
    boolean checkGebruikerSyntax(final String gebruikersnaam) {

        return syntaxValidator.isValidUser(gebruikersnaam);
    }

    /**
     * Bepaal of de syntax van een e-mailadres geldig is
     *
     * @param emailAddress
     * @return
     */
    boolean checkSyntax(final String emailAddress) {

        return syntaxValidator.isValid(emailAddress);
    }

    /**
     * Bepaal of de domeinnaam van een e-mailadres bestaat
     *
     * @param domainName domeinnaam
     * @return
     */
    boolean checkDomainExists(final String domainName) {

        try {
            Address.getByName(domainName);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * Bepaal of er voor de domeinnaam een MX-record wordt gevonden; m.a.w. domein is e-mailbaar.
     *
     * @param domainName domeinnaam
     * @return
     */
    boolean checkDomainHasMX(final String domainName) {

        try {
            final var records = new Lookup(domainName, Type.MX).run();
            return records != null && records.length > 0;
        } catch (TextParseException e) {
            return false;
        }
    }

    String getDomainName(final String emailAddress) {

        final var offset = emailAddress.indexOf('@');
        return offset >= 0 && offset == emailAddress.lastIndexOf('@') ?
                emailAddress.substring(offset + 1) : null;
    }

    String getUser(final String emailAddress) {
        // Leidt de gebruikersnaam af als het complementaire deel van de domeinnaam
        final var domain = getDomainName(emailAddress);
        return domain == null ? null
                : emailAddress.substring(0, emailAddress.length() - domain.length() - 1);
    }

    /**
     * Geeft aan of een adres (domein of e-mail) van de eigen organisatie is.
     *
     * @param adres
     * @return
     */
    private boolean isMyOrg(final String adres) {

        final var lowerCaseAdres = adres.toLowerCase();
        return lowerCaseAdres.equals(myOrgDomain) ||
                lowerCaseAdres.endsWith("@" + myOrgDomain) ||
                lowerCaseAdres.endsWith("." + myOrgDomain);
    }

    /**
     * Controleer of het opgegeven e-mailadres van de eigen organisatie bestaat door deze op te
     * zoeken in de interne systemen (i.e. LDAP).
     *
     * @param emailAdres
     * @return
     */
    private boolean checkMyOrg(final String emailAdres) {

        return emailAccountLookup.findByEmailAdres(emailAdres).isPresent();
    }

    /**
     * Geeft aan of bij de validaties gebruik gemaakt zal worden van LDAP queries
     *
     * @return <code>true</code> indien LDAP queries beschikbaar zijn anders <code>false</code>
     */
    public boolean isAccountLookupAvailable() {

        return this.emailAccountLookup != null;
    }

    /**
     * Implementatie-specifieke klasse waaraan de syntactische controles worden gedelegeerd.
     */
    private static class EmailSyntaxValidator extends
            org.apache.commons.validator.routines.EmailValidator {

        protected EmailSyntaxValidator() {

            super(false);
        }

        @Override
        protected boolean isValidUser(final String user) {

            return super.isValidUser(user);
        }

        @Override
        protected boolean isValidDomain(final String domain) {

            return super.isValidDomain(domain);
        }
    }

    @PostConstruct
    private void initialize() {

        if (this.emailAccountLookup != null) {
            try {
                CompletableFuture.supplyAsync(() -> {
                    try {
                        this.emailAccountLookup.findByEmailAdres("any@" + myOrgDomain);
                    } catch (CommunicationException cx) {
                        LOGGER.error(
                                "EmailAccountLookup wordt uitgeschakeld vanwege een verbindingsfout",
                                cx);
                        this.emailAccountLookup = null;
                    }
                    return null;
                }).get(initializationTimeout, TimeUnit.SECONDS);
                // Voer check uit om implementatie bibliotheek te laten initialiseren
                checkDomainHasMX("gmail.com");
            } catch (Exception e) {
                LOGGER.warn("Initialisatiefout", e);
            }
        }
    }
}
