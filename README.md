# GeVal 20240729

Service voor het uitvoeren van Generieke Validaties via SOAP web service en via REST API web-service. 

## Werking
GeVal is bedoeld als een generieke service voor specifieke validaties. Momenteel biedt GeVal de volgende soorten validaties aan:

•	e-mail voor de controle van e-mailadressen

Clients sturen een generiek gestructureerd vraagbericht naar de web service met daarin één of meer te valideren gegevens. Per te valideren gegeven wordt aangegeven welk type validatie uitgevoerd dient te worden. Indien de web service probleemloos de gevraagde validaties heeft kunnen verrichten, krijgen de clients een antwoordbericht retour met daarin per gevalideerd gegeven het resultaat van de validatie. 

GeVal is initieel ontwikkeld door Gemeente Rotterdam. Zie voor verdere toelichting de “doc” directory. 
Gemeente Rotterdam draagt geen enkele verantwoordelijkheid voor de werking van de code, onderhoudt de code niet (ook geen bug fixing). De code is gescreend om specifieke informatie over Rotterdam uit code of configuratie weg te halen zoals persoonlijke info van ontwikkelaars als naam en e-mail en ip adressen of dns namen van servers van Rotterdam zoals de Exchange server of DNS servers.

Mocht je aanpassingen of aanvullingen maken op de code, dan stellen we het op prijs als je deze ook publiceert en documenteert

## Lokaal starten

Onder Windows:

 mvn clean install
 java -cp geval-jar\target\geval-jar-0.1-SNAPSHOT-exec.jar;geval-jar\src\test\resources org.springframework.boot.loader.JarLauncher

 curl -H "Content-type: application/json"  -X POST http://localhost:8080/service/geval/ -d "@doc\geval.json"
 {"checks":[{"type":"e-mail","gegeven":"gebruiker-at-rotterdam.nl","validatie":"fout","details":["INVALIDE_SYNTAX"]},{"type":"e-mail","gegeven":"gebruiker@rotterdam.nl","validatie":"goed","details":["DOMEIN_MET_EMAIL"]}]}


## Koppelvlakken

Het specificaties van het koppelvlak zijn te bekijken als je de app hebt opgestart en aanroept via URLs:

http://localhost:8080/service/geval/wsdl  (SOAP web service)

of

http://localhost:8080/service/geval/yaml  (REST API web service)
