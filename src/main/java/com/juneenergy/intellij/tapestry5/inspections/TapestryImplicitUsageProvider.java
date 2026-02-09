/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.inspections;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.juneenergy.intellij.tapestry5.util.Tapestry5Conventions;
import com.juneenergy.intellij.tapestry5.util.Tapestry5PluginHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Provides implicit usage detection for Tapestry classes with linked templates.
 *
 * <p>In Tapestry 5, page and component classes are used implicitly through their
 * associated TML template files. This provider marks such classes as "used" to
 * prevent false "unused class" warnings from IntelliJ's inspections.
 *
 * <p>A class is considered implicitly used if:
 * <ul>
 *     <li>It is a Tapestry element (page, component, or mixin)</li>
 *     <li>It has a corresponding TML template file</li>
 * </ul>
 *
 * @author June Energy
 * @since 1.0.0
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/implicit-usage-provider.html">Implicit Usage Provider</a>
 */
public class TapestryImplicitUsageProvider implements ImplicitUsageProvider {

    /**
     * Checks if the given element is implicitly used.
     *
     * <p>For Tapestry classes (pages, components, mixins), this method returns
     * {@code true} if there is a corresponding TML template file, indicating
     * that the class is used through the template.
     *
     * @param element the PSI element to check
     * @return {@code true} if the element is implicitly used by a TML template
     */
    @Override
    public boolean isImplicitUsage(@NotNull PsiElement element) {
        if (!(element instanceof PsiClass psiClass)) {
            return false;
        }

        // Check if this is a Tapestry element (page, component, or mixin)
        if (!Tapestry5Conventions.isTapestryElement(psiClass)) {
            return false;
        }

        // Check if there is a corresponding TML template
        Project project = element.getProject();
        VirtualFile tmlFile = Tapestry5PluginHelper.findTmlForClass(project, psiClass);

        return tmlFile != null;
    }

    /**
     * Checks if the given element is implicitly read.
     *
     * <p>This method always returns {@code false} as Tapestry classes are
     * implicitly used rather than implicitly read.
     *
     * @param element the PSI element to check
     * @return {@code false} always
     */
    @Override
    public boolean isImplicitRead(@NotNull PsiElement element) {
        return false;
    }

    /**
     * Checks if the given element is implicitly written.
     *
     * <p>This method always returns {@code false} as Tapestry classes are
     * implicitly used rather than implicitly written.
     *
     * @param element the PSI element to check
     * @return {@code false} always
     */
    @Override
    public boolean isImplicitWrite(@NotNull PsiElement element) {
        return false;
    }
}
