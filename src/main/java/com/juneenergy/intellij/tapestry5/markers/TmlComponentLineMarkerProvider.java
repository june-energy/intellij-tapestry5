/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.markers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.juneenergy.intellij.tapestry5.util.Tapestry5FileUtils;
import com.juneenergy.intellij.tapestry5.util.TapestryComponentResolver;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

/**
 * Provides line markers (gutter icons) for Tapestry component tags in TML files.
 *
 * <p>This provider adds clickable gutter icons for:
 * <ul>
 *     <li>Tapestry component tags (e.g., {@code <t:if>}, {@code <t:ui.Panel>})</li>
 *     <li>HTML tags with t:type attribute (e.g., {@code <div t:type="layout">})</li>
 *     <li>Mixin references in t:mixins attribute</li>
 * </ul>
 *
 * @author June Energy
 * @since 1.0.0
 */
public class TmlComponentLineMarkerProvider extends RelatedItemLineMarkerProvider {

    /** Icon for component class navigation. */
    private static final Icon COMPONENT_ICON = loadComponentIcon();

    /** Icon for mixin class navigation. */
    private static final Icon MIXIN_ICON = loadMixinIcon();

    /**
     * Loads the component icon for the gutter marker.
     */
    private static Icon loadComponentIcon() {
        try {
            return com.intellij.icons.AllIcons.Nodes.Class;
        } catch (Exception e) {
            return com.intellij.icons.AllIcons.FileTypes.Java;
        }
    }

    /**
     * Loads the mixin icon for the gutter marker.
     */
    private static Icon loadMixinIcon() {
        try {
            return com.intellij.icons.AllIcons.Nodes.AbstractClass;
        } catch (Exception e) {
            return com.intellij.icons.AllIcons.FileTypes.Java;
        }
    }

    /**
     * Collects navigation markers for Tapestry component and mixin references.
     *
     * @param element the PSI element to check
     * @param result the collection to add markers to
     */
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                           @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // Check if we're in a TML file
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (!Tapestry5FileUtils.isTmlFile(virtualFile)) {
            return;
        }

        // Process XML tokens (for tag names)
        if (element instanceof XmlToken xmlToken) {
            if (xmlToken.getTokenType() == XmlTokenType.XML_NAME) {
                processTagNameToken(xmlToken, result);
            }
        }
        
        // Process XmlAttribute for t:type and t:mixins
        if (element instanceof XmlAttribute xmlAttribute) {
            processAttribute(xmlAttribute, result);
        }
    }

    /**
     * Processes an XML tag name token to check for Tapestry component reference.
     */
    private void processTagNameToken(@NotNull XmlToken xmlToken,
                                     @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        PsiElement parent = xmlToken.getParent();
        if (!(parent instanceof XmlTag xmlTag)) {
            return;
        }

        // Check if this token is the tag name
        String tagName = xmlTag.getName();
        String tokenText = xmlToken.getText();
        
        // Only process if this is the opening tag name
        if (!tokenText.equals(tagName) && !tokenText.equals(xmlTag.getLocalName())) {
            return;
        }

        // Get the component name
        String componentName = TapestryComponentResolver.getComponentName(xmlTag);
        if (componentName == null) {
            return;
        }

        // Skip if this is just from t:type attribute (handled separately)
        if (!tagName.startsWith("t:")) {
            return;
        }

        // Resolve the component
        Project project = xmlToken.getProject();
        PsiClass componentClass = TapestryComponentResolver.resolveComponent(project, componentName);

        if (componentClass != null) {
            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                    .create(COMPONENT_ICON)
                    .setTarget(componentClass)
                    .setTooltipText("Navigate to component: " + componentClass.getName());

            result.add(builder.createLineMarkerInfo(xmlToken));
        }
    }

    /**
     * Processes an XML attribute for t:type and t:mixins references.
     */
    private void processAttribute(@NotNull XmlAttribute attribute,
                                  @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        String attrName = attribute.getName();
        String attrValue = attribute.getValue();
        
        if (attrValue == null || attrValue.isEmpty()) {
            return;
        }

        Project project = attribute.getProject();

        // Handle t:type attribute
        if ("t:type".equals(attrName)) {
            PsiClass componentClass = TapestryComponentResolver.resolveComponent(project, attrValue);
            if (componentClass != null) {
                PsiElement anchor = attribute.getValueElement();
                if (anchor != null) {
                    NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                            .create(COMPONENT_ICON)
                            .setTarget(componentClass)
                            .setTooltipText("Navigate to component: " + componentClass.getName());

                    result.add(builder.createLineMarkerInfo(anchor));
                }
            }
        }

        // Handle t:mixins attribute
        if ("t:mixins".equals(attrName)) {
            List<String> mixinNames = TapestryComponentResolver.getMixinNames(attribute.getParent());
            for (String mixinName : mixinNames) {
                PsiClass mixinClass = TapestryComponentResolver.resolveMixin(project, mixinName);
                if (mixinClass != null) {
                    PsiElement anchor = attribute.getValueElement();
                    if (anchor != null) {
                        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                                .create(MIXIN_ICON)
                                .setTarget(mixinClass)
                                .setTooltipText("Navigate to mixin: " + mixinClass.getName());

                        result.add(builder.createLineMarkerInfo(anchor));
                        break; // Only add one marker per attribute
                    }
                }
            }
        }
    }
}
