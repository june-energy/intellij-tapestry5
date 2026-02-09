/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.xml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A permissive XML namespace descriptor for Tapestry namespaces.
 *
 * <p>This descriptor allows any element and attribute under Tapestry namespaces,
 * preventing IntelliJ from showing "unknown tag" or "unregistered namespace" errors.
 *
 * <p>Tapestry components and parameters are dynamically named, so we cannot validate
 * them against a static schema. This descriptor simply accepts any tag/attribute.
 *
 * @author June Energy
 * @since 1.0.0
 */
public class TapestryPermissiveNSDescriptor implements XmlNSDescriptor {

    private final String namespaceUri;

    /**
     * Creates a new permissive namespace descriptor for the given namespace.
     *
     * @param namespaceUri the namespace URI
     */
    public TapestryPermissiveNSDescriptor(@NotNull String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    /**
     * Returns an element descriptor for any element in this namespace.
     *
     * @param tag the XML tag
     * @return a permissive element descriptor
     */
    @Override
    @Nullable
    public XmlElementDescriptor getElementDescriptor(@NotNull XmlTag tag) {
        // Check if the tag is in our namespace
        String tagNamespace = tag.getNamespace();
        if (namespaceUri.equals(tagNamespace) || 
            tag.getName().startsWith("t:") || 
            tag.getName().startsWith("p:")) {
            return new TapestryPermissiveElementDescriptor(tag, this);
        }
        return null;
    }

    /**
     * Returns the root element descriptors for this namespace.
     * Since Tapestry components can have any name, we return an empty array.
     *
     * @param document the XML document
     * @return empty array
     */
    @Override
    public XmlElementDescriptor @NotNull [] getRootElementsDescriptors(@Nullable XmlDocument document) {
        return XmlElementDescriptor.EMPTY_ARRAY;
    }

    /**
     * Returns the PSI element declaration for this namespace.
     *
     * @return null (no specific declaration element)
     */
    @Override
    @Nullable
    public PsiElement getDeclaration() {
        return null;
    }

    /**
     * Returns the namespace prefix (unused in this implementation).
     *
     * @param context the context element
     * @return null
     */
    @Override
    @Nullable
    public String getName(PsiElement context) {
        return null;
    }

    /**
     * Returns the full namespace name.
     *
     * @return the namespace URI
     */
    @Override
    @Nullable
    public String getName() {
        return namespaceUri;
    }

    /**
     * Initialize the descriptor (no-op).
     *
     * @param element the init element
     */
    @Override
    public void init(PsiElement element) {
        // No initialization needed
    }

    /**
     * Returns the dependencies for this descriptor.
     *
     * @return empty array
     */
    @Override
    public Object @NotNull [] getDependencies() {
        return new Object[0];
    }

    /**
     * Returns the XSD file for this namespace.
     *
     * @return null (no schema file)
     */
    @Override
    @Nullable
    public XmlFile getDescriptorFile() {
        return null;
    }

    /**
     * A permissive element descriptor that allows any attributes.
     */
    private static class TapestryPermissiveElementDescriptor implements XmlElementDescriptor {
        
        private final XmlTag tag;
        private final TapestryPermissiveNSDescriptor nsDescriptor;

        TapestryPermissiveElementDescriptor(@NotNull XmlTag tag, 
                                            @NotNull TapestryPermissiveNSDescriptor nsDescriptor) {
            this.tag = tag;
            this.nsDescriptor = nsDescriptor;
        }

        @Override
        public String getQualifiedName() {
            return tag.getName();
        }

        @Override
        public String getDefaultName() {
            return tag.getLocalName();
        }

        @Override
        public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
            return XmlElementDescriptor.EMPTY_ARRAY;
        }

        @Override
        @Nullable
        public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
            // Allow any child element
            return new TapestryPermissiveElementDescriptor(childTag, nsDescriptor);
        }

        @Override
        public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
            if (context == null) {
                return XmlAttributeDescriptor.EMPTY;
            }
            // Return descriptors for all attributes on this tag
            XmlAttribute[] attributes = context.getAttributes();
            XmlAttributeDescriptor[] descriptors = new XmlAttributeDescriptor[attributes.length];
            for (int i = 0; i < attributes.length; i++) {
                descriptors[i] = new AnyXmlAttributeDescriptor(attributes[i].getName());
            }
            return descriptors;
        }

        @Override
        @Nullable
        public XmlAttributeDescriptor getAttributeDescriptor(@NotNull String attributeName, @Nullable XmlTag context) {
            // Allow any attribute
            return new AnyXmlAttributeDescriptor(attributeName);
        }

        @Override
        @Nullable
        public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
            return new AnyXmlAttributeDescriptor(attribute.getName());
        }

        @Override
        @Nullable
        public XmlNSDescriptor getNSDescriptor() {
            return nsDescriptor;
        }

        @Override
        @Nullable
        public XmlElementsGroup getTopGroup() {
            return null;
        }

        @Override
        public int getContentType() {
            return CONTENT_TYPE_ANY;
        }

        @Override
        @Nullable
        public String getDefaultValue() {
            return null;
        }

        @Override
        @Nullable
        public PsiElement getDeclaration() {
            return tag;
        }

        @Override
        @Nullable
        public String getName(PsiElement context) {
            return tag.getLocalName();
        }

        @Override
        @Nullable
        public String getName() {
            return tag.getLocalName();
        }

        @Override
        public void init(PsiElement element) {
            // No initialization needed
        }

        @Override
        public Object @NotNull [] getDependencies() {
            return new Object[] { tag };
        }
    }
}
