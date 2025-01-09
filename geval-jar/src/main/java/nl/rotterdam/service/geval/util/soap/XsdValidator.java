/*
 * Copyright 2024, Gemeente Rotterdam, Nederland
 * All rights reserved. Without explicit written consent beforehand of the gemeente Rotterdam nothing of this software and source code may be reproduced, adapted, distributed, and/or communicated to the public, except in case of a statutory limitation of copyright.
 */

package nl.rotterdam.service.geval.util.soap;

import java.io.StringReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import nl.rotterdam.service.geval.util.IOUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.xml.sax.SAXException;

/**
 * Thread-safe XSD validator
 */
public class XsdValidator {

    private final Validator validator;

    public XsdValidator(final String... xsdResourcePath) throws SAXException {

        assert xsdResourcePath != null && xsdResourcePath.length > 0;
        final var factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        final var sources = new Source[xsdResourcePath.length];
        for (int i = 0; i < xsdResourcePath.length; i++) {
            sources[i] = new StreamSource(
                    IOUtils.getResourceAsUrl(xsdResourcePath[i], XsdValidator.class)
                            .toExternalForm());
        }
        this.validator = factory.newSchema(sources).newValidator();
    }

    public synchronized boolean validate(final Message<?> message) throws MessagingException {

        try {
            validator.validate(new StreamSource(new StringReader((String) message.getPayload())));
        } catch (Exception e) {
            throw new MessagingException("Error while validating", e);
        }
        return true;
    }
}
