/*
 * Copyright 2024, Gemeente Rotterdam, Nederland
 * All rights reserved. Without explicit written consent beforehand of the gemeente Rotterdam nothing of this software and source code may be reproduced, adapted, distributed, and/or communicated to the public, except in case of a statutory limitation of copyright.
 */

package nl.rotterdam.service.geval.util.soap;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SoapUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SoapUtil.class);

    /**
     * Verwijdert de SOAP envelope.
     *
     * @param soapDocument
     * @return het document excl. de SOAP envelope
     */
    public static Document removeSoapEnvelope(final Document soapDocument) {

        try {
            final var xpath = getXpath();
            final var soapBody = (Node) xpath.evaluate(
                    "//" + DefaultNamespaces.SOAPENVELOPE11.getPrefix() + ":Body | " + "//"
                            + DefaultNamespaces.SOAPENVELOPE12.getPrefix() + ":Body",
                    soapDocument, XPathConstants.NODE);
            if (soapBody != null) {
                final var childNodes = soapBody.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        final var nonSoapDocument = newDocument();
                        final var importNode = nonSoapDocument.importNode(childNodes.item(i), true);
                        nonSoapDocument.appendChild(importNode);
                        return nonSoapDocument;
                    }
                }
            }
        } catch (XPathExpressionException e) {
            LOG.error("Soap body ophalen uit bericht mislukt", e);
        }
        return soapDocument;
    }

    /**
     * @param doc           the document to wrap in a SOAP envelope
     * @param soapNamespace {@link DefaultNamespaces#SOAPENVELOPE11} of
     *                      {@link DefaultNamespaces#SOAPENVELOPE12}
     * @return The SOAPified document
     */
    public static Document addSoapEnvelope(final Document doc,
            final DefaultNamespaces soapNamespace) {

        String xslFile;
        switch (soapNamespace) {
            case SOAPENVELOPE11:
                xslFile = "add_soap_envelope11.xsl";
                break;
            case SOAPENVELOPE12:
                xslFile = "add_soap_envelope12.xsl";
                break;
            default:
                throw new IllegalArgumentException(
                        "Alleen SOAPENVELOPE11 en SOAPENVELOPE12 zijn geldige parameters");
        }
        return transform(doc, xslFile, null);
    }

    /**
     * Plaatst een SOAP envelope met de correcte namespace en een SOAP fault met de code en
     * omschrijving.
     *
     * @param doc           wordt in het details veld van de SOAP fault geplaatst
     * @param soapFaultCode De faultcode. Aan de hand hiervan wordt de SOAP versie bepaald.
     * @param omschrijving  wordt in het reason veld geplaatst
     * @return een document met de correcte SOAP envelope en de foutmelding op de correcte plek
     */
    public static Document generateSoapFault(final Document doc, final SoapFaultCodes soapFaultCode,
            final String omschrijving) {

        String xslFile;
        switch (soapFaultCode.getSoapVersion()) {
            case SOAPENVELOPE11:
                xslFile = "add_soap_envelope11-with-soap-fault.xsl";
                break;
            case SOAPENVELOPE12:
                xslFile = "add_soap_envelope12-with-soap-fault.xsl";
                break;
            default:
                throw new IllegalArgumentException(
                        "Alleen SOAPENVELOPE11 en SOAPENVELOPE12 zijn geldige parameters");
        }
        final var params = new Hashtable<String, String>();
        params.put("code", soapFaultCode.getFaultCode());
        params.put("omschrijving", omschrijving);
        return transform(doc, xslFile, params);
    }

    private static Document transform(final Document doc,
            final String xslFile,
            final Map<String, String> params) {

        try {
            final var resultWriter = new StringWriter();
            final var factory = TransformerFactory.newInstance();
            final var transformer = factory
                    .newTransformer(new StreamSource(SoapUtil.class.getResourceAsStream(xslFile)));
            if (params != null) {
                for (Entry<String, String> param : params.entrySet()) {
                    transformer.setParameter(param.getKey(), param.getValue());
                }
            }
            transformer.transform(new DOMSource(doc), new StreamResult(resultWriter));
            return parse(resultWriter.toString());
        } catch (TransformerException exc) {
            throw new IllegalStateException("Transformeren xml mislukt: " + exc.getMessage(), exc);
        }
    }

    public static XPath getXpath() {

        final var xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new DefaultNamespaceContext());
        return xpath;
    }

    public static Document parse(String xmlString) {

        try {
            return getDocumentBuilderFactory().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlString)));
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new IllegalArgumentException("invalid xml: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("failed to read from String input: " + e.getMessage(),
                    e);
        }
    }

    public static Document newDocument() {

        try {
            return getDocumentBuilderFactory().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String toString(final Node documentOrNode) {

        return toString(documentOrNode, true);
    }

    private static String toString(final Node document, boolean prettyPrint) {

        try {
            DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) reg.getDOMImplementation("LS");
            LSSerializer serializer = impl.createLSSerializer();
            if (prettyPrint) {
                serializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            }
            LSOutput output = impl.createLSOutput();
            output.setEncoding("UTF-8");
            output.setCharacterStream(new StringWriter());
            serializer.write(document, output);
            return output.getCharacterStream().toString();
        } catch (Exception exc) {
            throw new IllegalStateException("failed to create DOMImplementationRegistry", exc);
        }
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory() {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    }

    public enum SoapFaultCodes {
        VERSION_MISMATCH_11("VersionMismatch", DefaultNamespaces.SOAPENVELOPE11),
        MUST_UNDERSTAND_11("MustUnderstand", DefaultNamespaces.SOAPENVELOPE11),
        CLIENT("Client", DefaultNamespaces.SOAPENVELOPE11),
        SERVER("Server", DefaultNamespaces.SOAPENVELOPE11),
        VERSION_MISMATCH_12("VersionMismatch", DefaultNamespaces.SOAPENVELOPE12),
        MUST_UNDERSTAND_12("MustUnderstand", DefaultNamespaces.SOAPENVELOPE12),
        DATA_ENCODING_UNKNOWN("DataEncodingUnknown", DefaultNamespaces.SOAPENVELOPE12),
        SENDER("Sender", DefaultNamespaces.SOAPENVELOPE12),
        RECEIVER("Receiver", DefaultNamespaces.SOAPENVELOPE12);
        private final String faultCode;
        private final DefaultNamespaces soapVersion;

        SoapFaultCodes(final String faultCode, final DefaultNamespaces soapVersion) {

            this.faultCode = faultCode;
            this.soapVersion = soapVersion;
        }

        public String getFaultCode() {

            return faultCode;
        }

        public DefaultNamespaces getSoapVersion() {

            return soapVersion;
        }
    }
}
