
/*
 * Copyright 2024, Gemeente Rotterdam, Nederland
 * All rights reserved. Without explicit written consent beforehand of the gemeente Rotterdam nothing of this software and source code may be reproduced, adapted, distributed, and/or communicated to the public, except in case of a statutory limitation of copyright.
 */

package nl.rotterdam.service.geval.util.soap;

public enum DefaultNamespaces {
    SOAP("soap", "http://schemas.xmlsoap.org/wsdl/soap/"),
    SOAPENVELOPE11("soapenv11", "http://schemas.xmlsoap.org/soap/envelope/"),
    SOAPENVELOPE12("soapenv12", "http://www.w3.org/2003/05/soap-envelope"),
    WSDL("wsdl", "http://schemas.xmlsoap.org/wsdl/"),
    XSI("xsi", "http://www.w3.org/2001/XMLSchema-instance"),
    XS("xs", "http://www.w3.org/2001/XMLSchema");

    private String prefix;

    private String namespace;

    DefaultNamespaces(String prefix, String namespace) {
        this.prefix = prefix;
        this.namespace = namespace;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNamespace() {
        return namespace;
    }
}