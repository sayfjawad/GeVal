package nl.rotterdam.service.geval.ws.controller;

import javax.servlet.http.HttpServletRequest;

import nl.rotterdam.service.geval.util.soap.WsdlSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WsdlController {
    private static final Logger LOG = LoggerFactory.getLogger(WsdlController.class);

    @Value("${geval.external.url}")
    private String externalUrl;

    // deze opzet maakt supplier niet mockbaar
    // waardoor de testbaarheid verhinderd wordt
    private final WsdlSupplier supplier = new WsdlSupplier();

    public WsdlController() {
        supplier.setMainWsdlFile("wsdl/geval-service.wsdl");
    }

    // request wordt niet gebruikt en is ook dode code
    // het gebruiken van Ternary operators op deze wijze
    // vermindert de leesbaarheid van de code
    @RequestMapping(path = "/wsdl", produces = "text/xml")
    public String supplyWsdlRoot(HttpServletRequest request, @RequestParam(value = "file", required = false) String file) throws Exception {
        return supplier.getWsdl(file == null || file.isEmpty() ? null : file, externalUrl);
    }
}
