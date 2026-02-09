/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.juneenergy.intellij.tapestry5.util.TapestryComponentResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PSI reference for Tapestry component references in TML files.
 *
 * <p>This reference enables click-through navigation from component tags
 * and t:type attributes to their corresponding Java class definitions.
 *
 * @author June Energy
 * @since 1.0.0
 */
public class TapestryComponentReference extends PsiReferenceBase<PsiElement> {

    private final String componentName;

    /**
     * Creates a new component reference.
     *
     * @param element the PSI element containing the reference
     * @param componentName the name of the component being referenced
     * @param rangeInElement the text range within the element
     */
    public TapestryComponentReference(@NotNull PsiElement element,
                                      @NotNull String componentName,
                                      @NotNull TextRange rangeInElement) {
        super(element, rangeInElement);
        this.componentName = componentName;
    }

    /**
     * Creates a new component reference with the full element range.
     *
     * @param element the PSI element containing the reference
     * @param componentName the name of the component being referenced
     */
    public TapestryComponentReference(@NotNull PsiElement element, @NotNull String componentName) {
        super(element);
        this.componentName = componentName;
    }

    /**
     * Resolves the reference to the target Java class.
     *
     * @return the resolved PsiClass, or {@code null} if not found
     */
    @Override
    @Nullable
    public PsiElement resolve() {
        return TapestryComponentResolver.resolveComponent(
                myElement.getProject(),
                componentName
        );
    }

    /**
     * Returns variants for code completion.
     * Currently returns an empty array; completion is handled separately.
     *
     * @return empty array (completion handled by separate contributor)
     */
    @Override
    public Object @NotNull [] getVariants() {
        // Completion is handled by a separate completion contributor
        return new Object[0];
    }

    /**
     * Gets the component name this reference points to.
     *
     * @return the component name
     */
    @NotNull
    public String getComponentName() {
        return componentName;
    }
}
