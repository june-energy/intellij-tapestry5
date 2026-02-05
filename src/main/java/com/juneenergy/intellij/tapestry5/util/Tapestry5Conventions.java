/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifierList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for Tapestry 5.x convention detection and resolution.
 *
 * <p>Tapestry 5 uses a convention-over-configuration approach where the type
 * of a class (page, component, mixin) is determined by:
 * <ul>
 *     <li>Package location: classes in {@code pages}, {@code components}, or {@code mixins} packages</li>
 *     <li>Annotations: {@code @Page}, {@code @Component}, etc.</li>
 * </ul>
 *
 * <h2>Supported Tapestry Annotations</h2>
 * <ul>
 *     <li>{@code org.apache.tapestry5.annotations.Page} - Marks a page class</li>
 *     <li>{@code org.apache.tapestry5.annotations.Component} - Embeds a component</li>
 *     <li>{@code org.apache.tapestry5.annotations.Property} - Auto-generates getter/setter</li>
 *     <li>{@code org.apache.tapestry5.annotations.Parameter} - Component parameter</li>
 *     <li>{@code org.apache.tapestry5.annotations.InjectComponent} - Injects embedded component</li>
 *     <li>{@code org.apache.tapestry5.annotations.InjectPage} - Injects another page</li>
 * </ul>
 *
 * @author June Energy
 * @since 1.0.0
 * @see <a href="https://tapestry.apache.org/conventions.html">Tapestry Conventions</a>
 */
public final class Tapestry5Conventions {

    // Package name conventions
    /** The package name segment for Tapestry pages. */
    public static final String PAGES_PACKAGE = "pages";

    /** The package name segment for Tapestry components. */
    public static final String COMPONENTS_PACKAGE = "components";

    /** The package name segment for Tapestry mixins. */
    public static final String MIXINS_PACKAGE = "mixins";

    /** The package name segment for Tapestry services. */
    public static final String SERVICES_PACKAGE = "services";

    // Tapestry annotation qualified names
    /** Fully qualified name of the @Page annotation. */
    public static final String PAGE_ANNOTATION = "org.apache.tapestry5.annotations.Page";

    /** Fully qualified name of the @Component annotation. */
    public static final String COMPONENT_ANNOTATION = "org.apache.tapestry5.annotations.Component";

    /** Fully qualified name of the @Property annotation. */
    public static final String PROPERTY_ANNOTATION = "org.apache.tapestry5.annotations.Property";

    /** Fully qualified name of the @Parameter annotation. */
    public static final String PARAMETER_ANNOTATION = "org.apache.tapestry5.annotations.Parameter";

    /** Fully qualified name of the @InjectComponent annotation. */
    public static final String INJECT_COMPONENT_ANNOTATION = "org.apache.tapestry5.annotations.InjectComponent";

    /** Fully qualified name of the @InjectPage annotation. */
    public static final String INJECT_PAGE_ANNOTATION = "org.apache.tapestry5.annotations.InjectPage";

    /** Fully qualified name of the @Persist annotation. */
    public static final String PERSIST_ANNOTATION = "org.apache.tapestry5.annotations.Persist";

    /** Fully qualified name of the @Cached annotation. */
    public static final String CACHED_ANNOTATION = "org.apache.tapestry5.annotations.Cached";

    /** Fully qualified name of the @OnEvent annotation. */
    public static final String ON_EVENT_ANNOTATION = "org.apache.tapestry5.annotations.OnEvent";

    private Tapestry5Conventions() {
        // Utility class - prevent instantiation
    }

    /**
     * Represents the type of Tapestry element.
     */
    public enum TapestryElementType {
        /** A Tapestry page. */
        PAGE,
        /** A Tapestry component. */
        COMPONENT,
        /** A Tapestry mixin. */
        MIXIN,
        /** Not a Tapestry element. */
        NONE
    }

    /**
     * Determines the Tapestry element type for a given Java class.
     *
     * <p>Detection is performed in the following order:
     * <ol>
     *     <li>Check for explicit annotations ({@code @Page}, etc.)</li>
     *     <li>Check package location conventions</li>
     * </ol>
     *
     * @param psiClass the Java class to check
     * @return the type of Tapestry element, or {@link TapestryElementType#NONE} if not a Tapestry element
     */
    @NotNull
    public static TapestryElementType detectElementType(@Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return TapestryElementType.NONE;
        }

        // Check for @Page annotation
        if (hasAnnotation(psiClass, PAGE_ANNOTATION)) {
            return TapestryElementType.PAGE;
        }

        // Check package location
        String qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName == null) {
            return TapestryElementType.NONE;
        }

        if (isInPagesPackage(qualifiedName)) {
            return TapestryElementType.PAGE;
        }

        if (isInComponentsPackage(qualifiedName)) {
            return TapestryElementType.COMPONENT;
        }

        if (isInMixinsPackage(qualifiedName)) {
            return TapestryElementType.MIXIN;
        }

        return TapestryElementType.NONE;
    }

    /**
     * Checks if the given class is a Tapestry page.
     *
     * <p>A class is considered a page if:
     * <ul>
     *     <li>It has the {@code @Page} annotation, or</li>
     *     <li>It is located in a {@code pages} package</li>
     * </ul>
     *
     * @param psiClass the class to check
     * @return {@code true} if the class is a Tapestry page
     */
    public static boolean isPage(@Nullable PsiClass psiClass) {
        return detectElementType(psiClass) == TapestryElementType.PAGE;
    }

    /**
     * Checks if the given class is a Tapestry component.
     *
     * <p>A class is considered a component if it is located in a {@code components} package.
     *
     * @param psiClass the class to check
     * @return {@code true} if the class is a Tapestry component
     */
    public static boolean isComponent(@Nullable PsiClass psiClass) {
        return detectElementType(psiClass) == TapestryElementType.COMPONENT;
    }

    /**
     * Checks if the given class is a Tapestry mixin.
     *
     * <p>A class is considered a mixin if it is located in a {@code mixins} package.
     *
     * @param psiClass the class to check
     * @return {@code true} if the class is a Tapestry mixin
     */
    public static boolean isMixin(@Nullable PsiClass psiClass) {
        return detectElementType(psiClass) == TapestryElementType.MIXIN;
    }

    /**
     * Checks if the given class is any type of Tapestry element (page, component, or mixin).
     *
     * @param psiClass the class to check
     * @return {@code true} if the class is a Tapestry element
     */
    public static boolean isTapestryElement(@Nullable PsiClass psiClass) {
        return detectElementType(psiClass) != TapestryElementType.NONE;
    }

    /**
     * Checks if the fully qualified class name indicates a pages package.
     *
     * @param qualifiedName the fully qualified class name
     * @return {@code true} if the class is in a pages package
     */
    public static boolean isInPagesPackage(@Nullable String qualifiedName) {
        return isInPackage(qualifiedName, PAGES_PACKAGE);
    }

    /**
     * Checks if the fully qualified class name indicates a components package.
     *
     * @param qualifiedName the fully qualified class name
     * @return {@code true} if the class is in a components package
     */
    public static boolean isInComponentsPackage(@Nullable String qualifiedName) {
        return isInPackage(qualifiedName, COMPONENTS_PACKAGE);
    }

    /**
     * Checks if the fully qualified class name indicates a mixins package.
     *
     * @param qualifiedName the fully qualified class name
     * @return {@code true} if the class is in a mixins package
     */
    public static boolean isInMixinsPackage(@Nullable String qualifiedName) {
        return isInPackage(qualifiedName, MIXINS_PACKAGE);
    }

    /**
     * Checks if a class has a specific annotation.
     *
     * @param psiClass the class to check
     * @param annotationFqn the fully qualified name of the annotation
     * @return {@code true} if the class has the annotation
     */
    public static boolean hasAnnotation(@Nullable PsiClass psiClass, @NotNull String annotationFqn) {
        if (psiClass == null) {
            return false;
        }
        PsiModifierList modifierList = psiClass.getModifierList();
        if (modifierList == null) {
            return false;
        }
        PsiAnnotation annotation = modifierList.findAnnotation(annotationFqn);
        return annotation != null;
    }

    /**
     * Extracts the package name from a fully qualified class name.
     *
     * @param qualifiedName the fully qualified class name
     * @return the package name, or empty string if no package
     */
    @NotNull
    public static String getPackageName(@Nullable String qualifiedName) {
        if (qualifiedName == null) {
            return "";
        }
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        return qualifiedName.substring(0, lastDot);
    }

    /**
     * Extracts the simple class name from a fully qualified class name.
     *
     * @param qualifiedName the fully qualified class name
     * @return the simple class name
     */
    @NotNull
    public static String getSimpleClassName(@Nullable String qualifiedName) {
        if (qualifiedName == null) {
            return "";
        }
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot < 0) {
            return qualifiedName;
        }
        return qualifiedName.substring(lastDot + 1);
    }

    /**
     * Checks if the qualified name is in a specific package.
     *
     * @param qualifiedName the fully qualified class name
     * @param packageSegment the package segment to check for
     * @return {@code true} if the class is in the specified package
     */
    private static boolean isInPackage(@Nullable String qualifiedName, @NotNull String packageSegment) {
        if (qualifiedName == null) {
            return false;
        }
        // Check for .pages. or .components. or .mixins. in the package name
        String pattern = "." + packageSegment + ".";
        return qualifiedName.contains(pattern) || qualifiedName.startsWith(packageSegment + ".");
    }
}
