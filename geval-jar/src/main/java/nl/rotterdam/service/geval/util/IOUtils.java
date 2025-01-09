/*
 * Copyright 2024, Gemeente Rotterdam, Nederland
 * All rights reserved. Without explicit written consent beforehand of the gemeente Rotterdam nothing of this software and source code may be reproduced, adapted, distributed, and/or communicated to the public, except in case of a statutory limitation of copyright.
 */

package nl.rotterdam.service.geval.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IOUtils {

    private static final Log LOG = LogFactory.getLog(IOUtils.class);

    /**
     * Haal een resource op door deze eerst op te zoeken in het bestandssysteem , vervolgens van een
     * URL, of uiteindelijk in de classpath.
     *
     * @param resourceName naam/pad van de resource
     * @param clazz        klasse van de aanroeper
     * @return URL van resource of <code>null</code> indien niet gevonden
     */
    public static URL getResourceAsUrl(final String resourceName, final Class clazz) {

        if (resourceName == null) {
            throw new IllegalArgumentException("Resource name is null");
        }
        URL url = null;
        // Zoek eerst op het bestandssysteem
        try {
            final var file = new File(resourceName);
            if (file.exists()) {
                url = file.getAbsoluteFile().toURL();
            } else {
                LOG.debug(
                        "Unable to load resource from the file system: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            LOG.debug("Unable to load resource from the file system: " + e.getMessage());
        }
        // Zoek op de classpath.
        if (url == null) {
            try {
                url = (URL) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {

                        return clazz.getClassLoader().getResource(resourceName);
                    }
                });
                if (url == null) {
                    LOG.debug("Unable to load resource " + resourceName + " from the classpath");
                }
            } catch (Exception e) {
                LOG.debug("Unable to load resource " + resourceName + " from the classpath: "
                        + e.getMessage());
            }
        }
        if (url == null) {
            try {
                url = new URL(resourceName);
            } catch (MalformedURLException e) {
                // ignore
            }
        }
        return url;
    }
}
