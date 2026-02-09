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
 * PSI reference for Tapestry mixin references in TML files.
 *
 * <p>This reference enables click-through navigation from t:mixins attributes
 * to their corresponding Java class definitions.
 *
 * @author June Energy
 * @since 1.0.0
 */
public class TapestryMixinReference extends PsiReferenceBase<PsiElement> {

    private final String mixinName;

    /**
     * Creates a new mixin reference.
     *
     * @param element the PSI element containing the reference
     * @param mixinName the name of the mixin being referenced
     * @param rangeInElement the text range within the element
     */
    public TapestryMixinReference(@NotNull PsiElement element,
                                  @NotNull String mixinName,
                                  @NotNull TextRange rangeInElement) {
        super(element, rangeInElement);
        this.mixinName = mixinName;
    }

    /**
     * Resolves the reference to the target Java mixin class.
     *
     * @return the resolved PsiClass, or {@code null} if not found
     */
    @Override
    @Nullable
    public PsiElement resolve() {
        return TapestryComponentResolver.resolveMixin(
                myElement.getProject(),
                mixinName
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
        return new Object[0];
    }

    /**
     * Gets the mixin name this reference points to.
     *
     * @return the mixin name
     */
    @NotNull
    public String getMixinName() {
        return mixinName;
    }
}
