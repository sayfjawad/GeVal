package nl.rotterdam.service.geval.ws.controller;

import nl.rotterdam.service.geval.util.soap.WsdlSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WsdlController {

    @Value("${geval.external.url}")
    private String externalUrl;
    private final WsdlSupplier supplier = new WsdlSupplier();

    public WsdlController() {

        supplier.setMainWsdlFile("wsdl/geval-service.wsdl");
    }

    @RequestMapping(path = "/wsdl", produces = "text/xml")
    public String supplyWsdlRoot(final @RequestParam(value = "file", required = false) String file)
            throws Exception {

        return supplier.getWsdl(file == null || file.isEmpty() ? null : file, externalUrl);
    }
}
