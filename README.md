# GeVal 20240729

Service voor het uitvoeren van Generieke Validaties via SOAP web service en via REST API web-service.


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
