/*
 * Copyright 2024, Gemeente Rotterdam, Nederland
 * All rights reserved. Without explicit written consent beforehand of the gemeente Rotterdam nothing of this software and source code may be reproduced, adapted, distributed, and/or communicated to the public, except in case of a statutory limitation of copyright.
 */

package nl.rotterdam.service.geval.util.soap;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Verantwoordelijk voor het retourneren van de inhoud van WSDL bestanden waarbij verwijzingen naar
 * andere bestanden worden vervangen door URLs zodat de bestanden via HTTP zijn op te vragen.
 * Deze bestanden staan op de classpath en de mogelijke padverwijzingen naar andere bestanden
 * kunnen zowel relatief als absoluut zijn. Ook is het mogelijk dat de padverwijzingen voorzien
 * zijn van een 'classpath:xsd/' prefix. In dat geval is de padverwijzing absoluut.
 */
public class WsdlSupplier {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(WsdlSupplier.class);

    // Is dit thread-safe ?!
    private String mainWsdlFile = "";

    /**
     * Levert WSDL definitie aan op URL &lt;requestPath&gt;/wsdl[?file=bestandspad]
     *
     * @param bestandspad
     * @param requestPath  basis URL voor ophalen van bestand
     * @return inhoud van wsdl- of xsd-bestand met aangepaste URL's
     * @throws Exception
     */
    public String getWsdl(String bestandspad, String requestPath) throws Exception {
        if (isNotBlank(bestandspad)) {
            if (bestandspad.endsWith(".xsd") || bestandspad.endsWith(".wsdl")) {
                return processPayload(Paths.get(bestandspad), readResourceContentAsString(bestandspad), requestPath);
            } else {
                throw new IllegalArgumentException("Ongeldig bestand wordt opgevraagd:" + bestandspad);
            }
        } else {
            //hoofd wsdl wordt opgevraagd
            return processPayload(Paths.get(mainWsdlFile), readResourceContentAsString(mainWsdlFile), requestPath);
        }
    }

    private String processPayload(final Path currentFilePath, String payload, String requestPath) throws XPathExpressionException {
        Document doc = SoapUtil.parse(payload);
        XPath xPath = getXpath();

        NodeList importNodes = (NodeList) xPath.evaluate("//xs:import", doc, XPathConstants.NODESET);
        for (int i = 0; i < importNodes.getLength(); i++) {
            processXsImportOrIncludeNode(importNodes.item(i), currentFilePath, requestPath);
        }

        importNodes = (NodeList) xPath.evaluate("//wsdl:import", doc, XPathConstants.NODESET);
        for (int i = 0; i < importNodes.getLength(); i++) {
            processWsdlImportNode(importNodes.item(i), currentFilePath, requestPath);
        }

        NodeList includeNodes = (NodeList) xPath.evaluate("//xs:include", doc, XPathConstants.NODESET);
        for(int i = 0; i < includeNodes.getLength(); i++) {
            processXsImportOrIncludeNode(includeNodes.item(i), currentFilePath, requestPath);
        }

        Node addressNode = (Node) xPath.evaluate("//soap:address", doc, XPathConstants.NODE);
        if (addressNode != null) {
            addressNode.getAttributes().getNamedItem("location").setNodeValue(requestPath + "/wsdl");
        }

        return SoapUtil.toString(doc);
    }

    private void processXsImportOrIncludeNode(final Node item, final Path currentFilePath, final String requestPath) {
        final String location = translateLocation(item, "schemaLocation", currentFilePath, requestPath);
        item.getAttributes().getNamedItem("schemaLocation").setNodeValue(location);
    }

    private void processWsdlImportNode(final Node item, final Path currentFilePath, final String requestPath) {
        final String location = translateLocation(item, "location", currentFilePath, requestPath);
        item.getAttributes().getNamedItem("location").setNodeValue(location);
    }

    private String translateLocation(final Node item, final String locationTag, final Path currentFilePath, final String requestPath) {
        String location = item.getAttributes().getNamedItem(locationTag).getNodeValue();
        if (location.startsWith("classpath:")) {
            if (location.startsWith("classpath:xsd/")) {
                // exclude classpath prefix, paths are absolute
                location = location.substring(10);
            } else {
                LOGGER.error("Locatie in XSD of WSDL begint niet met xsd/");
            }
        } else {
            location = getFilePath(currentFilePath, location).toString().replace('\\', '/');
        }
        return requestPath + "/wsdl?file=" + location;
    }

    public void setMainWsdlFile(String mainWsdlFile) {
        this.mainWsdlFile = mainWsdlFile;
    }

    protected String readResourceContentAsString(String fileName) throws Exception {
        InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
        try {
            return IOUtils.toString(inStream, StandardCharsets.UTF_8);
        } catch(Exception exc) {
            LOGGER.error("Onbekende fout opgetreden bij het ophalen van een wsdl of xsd: " + fileName, exc);
            throw exc;
        }
    }

    private XPath getXpath() {
        XPath xpath = XPathFactory.newInstance().newXPath();
        final DefaultNamespaceContext nsc = new DefaultNamespaceContext() {
            {
                addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
                addNamespace("xs", "http://www.w3.org/2001/XMLSchema");
                addNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
            }
        };
        xpath.setNamespaceContext(nsc);
        return xpath;
    }

    /**
     * Bepaal nieuwe path naar een bestand ten opzichte van een huidig bestand.
     *
     * @param currentFilePath       path naar huidig bestand
     * @param newFilePath           absolute path of relatieve path ten opzichte van huidig bestand
     * @return
     */
    private Path getFilePath(final Path currentFilePath, final String newFilePath) {
        final Path path = isRelative(newFilePath) ?
                currentFilePath.getParent().resolve(newFilePath) : Paths.get(newFilePath);
        return path.normalize();
    }

    private boolean isAbsolute(final String path) {
        return path.startsWith(File.separator);
    }

    private boolean isRelative(final String path) {
        return !isAbsolute(path);
    }
}
