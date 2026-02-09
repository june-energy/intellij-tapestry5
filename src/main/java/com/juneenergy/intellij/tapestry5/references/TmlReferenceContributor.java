/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ProcessingContext;
import com.juneenergy.intellij.tapestry5.util.Tapestry5FileUtils;
import com.juneenergy.intellij.tapestry5.util.TapestryComponentResolver;
import org.jetbrains.annotations.NotNull;

/**
 * Reference contributor for Tapestry component and mixin references in TML files.
 *
 * <p>This contributor provides references for:
 * <ul>
 *     <li>Tapestry component tags (e.g., {@code <t:if>}, {@code <t:ui.Panel>})</li>
 *     <li>t:type attribute values (e.g., {@code t:type="layout"})</li>
 *     <li>t:mixins attribute values (e.g., {@code t:mixins="ui/confirmation"})</li>
 * </ul>
 *
 * @author June Energy
 * @since 1.0.0
 */
public class TmlReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        // Register reference provider for XML tag names (t:componentName)
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(XmlToken.class),
                new TapestryTagNameReferenceProvider()
        );

        // Register reference provider for t:type attribute values
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue()
                        .withParent(XmlPatterns.xmlAttribute().withName("t:type")),
                new TapestryTypeAttributeReferenceProvider()
        );

        // Register reference provider for t:mixins attribute values
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue()
                        .withParent(XmlPatterns.xmlAttribute().withName("t:mixins")),
                new TapestryMixinsAttributeReferenceProvider()
        );
    }

    /**
     * Reference provider for Tapestry component tag names.
     */
    private static class TapestryTagNameReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                @NotNull ProcessingContext context) {
            if (!(element instanceof XmlToken xmlToken)) {
                return PsiReference.EMPTY_ARRAY;
            }

            // Only process tag name tokens
            if (xmlToken.getTokenType() != XmlTokenType.XML_NAME) {
                return PsiReference.EMPTY_ARRAY;
            }

            // Check if we're in a TML file
            if (!Tapestry5FileUtils.isTmlFile(element.getContainingFile())) {
                return PsiReference.EMPTY_ARRAY;
            }

            // Get the parent tag
            PsiElement parent = xmlToken.getParent();
            if (!(parent instanceof XmlTag xmlTag)) {
                return PsiReference.EMPTY_ARRAY;
            }

            // Check if this token is the tag name (not attribute name)
            String tagName = xmlTag.getName();
            String tokenText = xmlToken.getText();
            
            // We want to match the component name part only
            if (!tagName.startsWith("t:") || !tokenText.equals(tagName)) {
                // Also check if this is just the local name part
                String localName = xmlTag.getLocalName();
                if (!tokenText.equals(localName) && !tokenText.equals(tagName)) {
                    return PsiReference.EMPTY_ARRAY;
                }
            }

            // Get component name from the tag
            String componentName = TapestryComponentResolver.getComponentName(xmlTag);
            if (componentName == null) {
                return PsiReference.EMPTY_ARRAY;
            }

            // Calculate the range for just the component name (after t:)
            int prefixLength = tagName.startsWith("t:") ? 2 : 0;
            TextRange range = new TextRange(prefixLength, tokenText.length());

            return new PsiReference[]{
                    new TapestryComponentReference(element, componentName, range)
            };
        }
    }

    /**
     * Reference provider for t:type attribute values.
     */
    private static class TapestryTypeAttributeReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                @NotNull ProcessingContext context) {
            if (!(element instanceof XmlAttributeValue attrValue)) {
                return PsiReference.EMPTY_ARRAY;
            }

            // Check if we're in a TML file
            if (!Tapestry5FileUtils.isTmlFile(element.getContainingFile())) {
                return PsiReference.EMPTY_ARRAY;
            }

            String value = attrValue.getValue();
            if (value == null || value.isEmpty()) {
                return PsiReference.EMPTY_ARRAY;
            }

            // Create reference for the entire value (which is the component type)
            // The range excludes the quotes
            TextRange valueRange = new TextRange(1, value.length() + 1);

            return new PsiReference[]{
                    new TapestryComponentReference(element, value, valueRange)
            };
        }
    }

    /**
     * Reference provider for t:mixins attribute values.
     * Handles comma-separated mixin names.
     */
    private static class TapestryMixinsAttributeReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                @NotNull ProcessingContext context) {
            if (!(element instanceof XmlAttributeValue attrValue)) {
                return PsiReference.EMPTY_ARRAY;
            }

            // Check if we're in a TML file
            if (!Tapestry5FileUtils.isTmlFile(element.getContainingFile())) {
                return PsiReference.EMPTY_ARRAY;
            }

            String value = attrValue.getValue();
            if (value == null || value.isEmpty()) {
                return PsiReference.EMPTY_ARRAY;
            }

            // Parse comma-separated mixin names
            String[] mixins = value.split(",");
            PsiReference[] references = new PsiReference[mixins.length];
            int currentOffset = 1; // Start after opening quote

            for (int i = 0; i < mixins.length; i++) {
                String mixinWithSpaces = mixins[i];
                String mixin = mixinWithSpaces.trim();
                
                // Calculate the actual position in the string
                int mixinStart = currentOffset + mixinWithSpaces.indexOf(mixin.charAt(0));
                int mixinEnd = mixinStart + mixin.length();
                
                TextRange range = new TextRange(mixinStart, mixinEnd);
                references[i] = new TapestryMixinReference(element, mixin, range);
                
                currentOffset += mixinWithSpaces.length() + 1; // +1 for comma
            }

            return references;
        }
    }
}
