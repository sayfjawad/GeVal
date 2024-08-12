package nl.rotterdam.service.geval;

/**
 * Klasse Standalone biedt de mogelijkheid om de 'generieke validatie' (GeVal) service lokaal uit
 * te voeren zonder JBoss. In plaats daarvan wordt er gebruik gemaakt van een embedded Jetty server.
 * Dit vergemakkelijkt het testen van de interactie met andere endpoints end/of systemen.
 * <p/>
 * De standalone versie heeft mogelijk een client-certificaat nodig om met een LDAP server verbinding
 * te kunnen maken. De benodigde instellingen kunnen als volgt worden meegegeven:
 *
 * <pre>
 * -Djavax.net.ssl.keyStoreType=jks
 * -Djavax.net.ssl.keyStore=keystore.jks
 * -Djavax.net.ssl.keyStorePassword=geheim
 * -Djavax.net.ssl.trustStoreType=jks
 * -Djavax.net.ssl.trustStore=truststore.jks
 * -Djavax.net.ssl.trustStorePassword=geheim
 * </pre>
 *
 * De certificaten die ontwikkelaars gebruiken om via hun browser naar ebs-test.ontwikkel.rotterdam.nl
 * of ebs-demo.ontwikkel.rotterdam.nl te gaan, zijn hiervoor geschikt.
 */
public class Standalone {
    public static void main(String[] args) {
        BrokerCreator.createForTesting();

        GevalApplication.main(args);
    }
}
