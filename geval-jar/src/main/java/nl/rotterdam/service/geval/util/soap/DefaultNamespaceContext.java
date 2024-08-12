
/*
 * Copyright 2024, Gemeente Rotterdam, Nederland
 * All rights reserved. Without explicit written consent beforehand of the gemeente Rotterdam nothing of this software and source code may be reproduced, adapted, distributed, and/or communicated to the public, except in case of a statutory limitation of copyright.
 */

package nl.rotterdam.service.geval.util.soap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class DefaultNamespaceContext implements NamespaceContext {

    private Map<String, Set<String>> prefixesByUri = new HashMap<>();

    private Map<String, String> uriByPrefix = new HashMap<>();

    private Set<String> predefinedPrefixes = new HashSet<>();

    public DefaultNamespaceContext() {
        addPredefinedNamespace(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
        addPredefinedNamespace(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        addPredefinedNamespace(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);

        for (DefaultNamespaces namespace : DefaultNamespaces.values()) {
            addNamespace(namespace.getPrefix(), namespace.getNamespace());
        }
    }

    private void addPredefinedNamespace(String prefix, String uri) {
        predefinedPrefixes.add(prefix);
        addPrefix(prefix, uri);
    }

    /**
     * Adds a namespace overriding any previous uri mapped to this prefix.
     *
     * @param prefix
     *            prefix
     * @param uri
     *            uri
     */
    protected void addNamespace(String prefix, String uri) {
        checkNotPredefined(prefix);
        removePrefix(prefix);
        addPrefix(prefix, uri);
    }

    private void checkNotPredefined(String prefix) {
        if (predefinedPrefixes.contains(prefix)) {
            throw new IllegalArgumentException("cannot override predefined prefix '" + prefix + "'");
        }
    }

    private void addPrefix(String prefix, String uri) {
        uriByPrefix.put(prefix, uri);
        getPrefixesSet(uri).add(prefix);
    }

    private void removePrefix(String prefix) {

        String uri = uriByPrefix.remove(prefix);

        if (uri != null) {
            prefixesByUri.get(uri).remove(prefix);
        }
    }

    @Override
    public String getNamespaceURI(String prefix) {

        if (prefix == null) {
            throw new IllegalArgumentException("null namespace");
        }

        return uriByPrefix.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
    }

    @Override
    public String getPrefix(String namespaceURI) {

        if (namespaceURI == null) {
            throw new IllegalArgumentException("null namespace");
        }

        Set<String> prefixes = prefixesByUri.get(namespaceURI);

        if (prefixes == null || prefixes.isEmpty()) {
            return null;
        }

        return prefixes.iterator().next();
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {

        if (namespaceURI == null) {
            throw new IllegalArgumentException("null namespace");
        }

        return Collections.unmodifiableSet(getPrefixesSet(namespaceURI)).iterator();
    }

    private Set<String> getPrefixesSet(String uri) {
        return prefixesByUri.computeIfAbsent(uri, k -> new HashSet<>());
    }
}
