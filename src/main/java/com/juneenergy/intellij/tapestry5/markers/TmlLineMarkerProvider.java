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
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.juneenergy.intellij.tapestry5.util.Tapestry5FileUtils;
import com.juneenergy.intellij.tapestry5.util.Tapestry5PluginHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

/**
 * Provides line markers (gutter icons) for TML template files that have corresponding Java classes.
 *
 * <p>When a TML template has an associated Java page/component class, this provider adds a
 * clickable gutter icon next to the root element that allows navigation to the Java class.
 *
 * @author June Energy
 * @since 1.0.0
 */
public class TmlLineMarkerProvider extends RelatedItemLineMarkerProvider {

    /** Icon for the Java class gutter marker. */
    private static final Icon JAVA_ICON = loadIcon();

    /**
     * Loads the Java icon for the gutter marker.
     *
     * @return the icon, or a default icon if loading fails
     */
    private static Icon loadIcon() {
        try {
            return com.intellij.icons.AllIcons.FileTypes.Java;
        } catch (Exception e) {
            return com.intellij.icons.AllIcons.FileTypes.Unknown;
        }
    }

    /**
     * Collects navigation markers for TML templates.
     *
     * @param element the PSI element to check
     * @param result the collection to add markers to
     */
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                           @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // Only process root XML tags in TML files
        if (!(element instanceof XmlTag xmlTag)) {
            return;
        }

        // Check if this is a TML file
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (!Tapestry5FileUtils.isTmlFile(virtualFile)) {
            return;
        }

        // Only add marker to the root element
        if (!(element.getParent() instanceof XmlDocument)) {
            return;
        }

        // Find the corresponding Java class
        Project project = element.getProject();
        PsiClass javaClass = Tapestry5PluginHelper.findClassForTml(project, virtualFile);

        if (javaClass != null) {
            // Create the line marker on the tag name
            PsiElement anchor = xmlTag.getFirstChild();
            if (anchor != null) {
                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                        .create(JAVA_ICON)
                        .setTarget(javaClass)
                        .setTooltipText("Navigate to Java class: " + javaClass.getName());

                result.add(builder.createLineMarkerInfo(anchor));
            }
        }
    }
}
