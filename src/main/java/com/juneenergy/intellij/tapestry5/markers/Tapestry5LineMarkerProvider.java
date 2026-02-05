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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiManager;
import com.juneenergy.intellij.tapestry5.util.Tapestry5Conventions;
import com.juneenergy.intellij.tapestry5.util.Tapestry5PluginHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

/**
 * Provides line markers (gutter icons) for Tapestry Java classes that have corresponding TML templates.
 *
 * <p>When a Java class is a Tapestry page, component, or mixin with an associated TML template,
 * this provider adds a clickable gutter icon next to the class declaration that allows
 * navigation to the template file.
 *
 * <h2>Supported Elements</h2>
 * <ul>
 *     <li>Pages: Classes in {@code pages} package</li>
 *     <li>Components: Classes in {@code components} package</li>
 *     <li>Mixins: Classes in {@code mixins} package</li>
 * </ul>
 *
 * @author June Energy
 * @since 1.0.0
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/line-marker-provider.html">Line Marker Provider</a>
 */
public class Tapestry5LineMarkerProvider extends RelatedItemLineMarkerProvider {

    /** Icon for the template file gutter marker. */
    private static final Icon TEMPLATE_ICON = loadIcon();

    /**
     * Loads the Tapestry icon for the gutter marker.
     *
     * @return the icon, or a default icon if loading fails
     */
    private static Icon loadIcon() {
        try {
            return com.intellij.icons.AllIcons.FileTypes.Html;
        } catch (Exception e) {
            // Fallback to a safe default
            return com.intellij.icons.AllIcons.FileTypes.Unknown;
        }
    }

    /**
     * Collects navigation markers for Tapestry elements.
     *
     * @param element the PSI element to check
     * @param result the collection to add markers to
     */
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                           @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // Only process class identifier (the class name)
        if (!(element instanceof PsiIdentifier)) {
            return;
        }

        PsiElement parent = element.getParent();
        if (!(parent instanceof PsiClass psiClass)) {
            return;
        }

        // Only process if this identifier is the class name identifier
        if (element != psiClass.getNameIdentifier()) {
            return;
        }

        // Check if this is a Tapestry element (page, component, or mixin)
        if (!Tapestry5Conventions.isTapestryElement(psiClass)) {
            return;
        }

        // Find the corresponding TML template
        Project project = element.getProject();
        VirtualFile tmlFile = Tapestry5PluginHelper.findTmlForClass(project, psiClass);

        if (tmlFile != null) {
            PsiFile tmlPsiFile = PsiManager.getInstance(project).findFile(tmlFile);
            if (tmlPsiFile != null) {
                // Create the line marker
                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                        .create(TEMPLATE_ICON)
                        .setTarget(tmlPsiFile)
                        .setTooltipText("Navigate to TML template: " + tmlFile.getName());

                result.add(builder.createLineMarkerInfo(element));
            }
        }
    }
}
