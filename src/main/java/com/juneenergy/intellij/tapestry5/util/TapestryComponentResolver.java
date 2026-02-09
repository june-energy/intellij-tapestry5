/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Utility class for resolving Tapestry component references in TML files.
 *
 * <p>This class handles the resolution of component references like:
 * <ul>
 *     <li>{@code t:if}, {@code t:loop} - Built-in Tapestry core components</li>
 *     <li>{@code t:grid}, {@code t:form} - Built-in Tapestry components</li>
 *     <li>{@code t:ui.FeedbackComponent} - Custom components in subpackages</li>
 *     <li>{@code t:type="layout"} - Component type attribute</li>
 * </ul>
 *
 * @author June Energy
 * @since 1.0.0
 */
public final class TapestryComponentResolver {

    /** The Tapestry namespace prefix for component tags. */
    public static final String TAPESTRY_NAMESPACE_PREFIX = "t";

    /** The Tapestry parameter namespace prefix. */
    public static final String PARAMETER_NAMESPACE_PREFIX = "p";

    /** The Tapestry 5.4 namespace URI. */
    public static final String TAPESTRY_NAMESPACE_URI = "http://tapestry.apache.org/schema/tapestry_5_4.xsd";

    /** The Tapestry parameter namespace URI. */
    public static final String PARAMETER_NAMESPACE_URI = "tapestry:parameter";

    /** Built-in Tapestry core component names mapped to their class names. */
    private static final Map<String, String> BUILT_IN_COMPONENTS = createBuiltInComponentsMap();

    /** The base package for Tapestry corelib components. */
    private static final String CORELIB_COMPONENTS_PACKAGE = "org.apache.tapestry5.corelib.components";

    /** The base package for Tapestry corelib mixins. */
    private static final String CORELIB_MIXINS_PACKAGE = "org.apache.tapestry5.corelib.mixins";

    private TapestryComponentResolver() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates the map of built-in Tapestry component names.
     */
    private static Map<String, String> createBuiltInComponentsMap() {
        Map<String, String> map = new HashMap<>();
        
        // Control flow components
        map.put("if", "If");
        map.put("unless", "Unless");
        map.put("delegate", "Delegate");
        map.put("block", "Block");
        map.put("body", "Body");
        map.put("renderobject", "RenderObject");
        
        // Loop components
        map.put("loop", "Loop");
        map.put("grid", "Grid");
        map.put("gridrows", "GridRows");
        map.put("gridcolumns", "GridColumns");
        map.put("gridcell", "GridCell");
        map.put("gridpager", "GridPager");
        
        // Form components
        map.put("form", "Form");
        map.put("textfield", "TextField");
        map.put("textarea", "TextArea");
        map.put("passwordfield", "PasswordField");
        map.put("checkbox", "Checkbox");
        map.put("radio", "Radio");
        map.put("radiogroup", "RadioGroup");
        map.put("select", "Select");
        map.put("submit", "Submit");
        map.put("hidden", "Hidden");
        map.put("label", "Label");
        map.put("errors", "Errors");
        map.put("datefield", "DateField");
        map.put("upload", "Upload");
        map.put("palette", "Palette");
        map.put("checklist", "Checklist");
        
        // Link components
        map.put("actionlink", "ActionLink");
        map.put("eventlink", "EventLink");
        map.put("pagelink", "PageLink");
        map.put("externallink", "ExternalLink");
        
        // Layout components
        map.put("zone", "Zone");
        map.put("any", "Any");
        map.put("output", "Output");
        map.put("outputraw", "OutputRaw");
        map.put("beandisplay", "BeanDisplay");
        map.put("beaneditform", "BeanEditForm");
        map.put("beaneditor", "BeanEditor");
        map.put("propertyeditor", "PropertyEditor");
        map.put("propertydisplay", "PropertyDisplay");
        
        // Alert components
        map.put("alerts", "Alerts");
        
        // Ajax components
        map.put("ajaxformloop", "AjaxFormLoop");
        map.put("addrowlink", "AddRowLink");
        map.put("removerowlink", "RemoveRowLink");
        map.put("formfragment", "FormFragment");
        map.put("trigger", "Trigger");
        map.put("progressivedisplay", "ProgressiveDisplay");
        map.put("kicker", "Kicker");
        
        // Other components
        map.put("tree", "Tree");
        map.put("dynamic", "Dynamic");
        map.put("devmodelcacheatusresetlink", "DevModeLocalCacheAtUsResetLink");
        
        return Collections.unmodifiableMap(map);
    }

    /**
     * Resolves a Tapestry component reference to its Java class.
     *
     * @param project the current project
     * @param componentName the component name (e.g., "if", "ui.FeedbackComponent", "ActionLink")
     * @return the resolved PsiClass, or {@code null} if not found
     */
    @Nullable
    public static PsiClass resolveComponent(@NotNull Project project, @NotNull String componentName) {
        if (componentName.isEmpty()) {
            return null;
        }

        // Normalize the component name (remove leading t: if present)
        String normalizedName = componentName;
        if (normalizedName.startsWith(TAPESTRY_NAMESPACE_PREFIX + ":")) {
            normalizedName = normalizedName.substring(2);
        }

        // First, try to resolve as a built-in component
        PsiClass builtInClass = resolveBuiltInComponent(project, normalizedName);
        if (builtInClass != null) {
            return builtInClass;
        }

        // Try to resolve as a project component
        return resolveProjectComponent(project, normalizedName);
    }

    /**
     * Resolves a built-in Tapestry component.
     *
     * @param project the current project
     * @param componentName the component name (case-insensitive)
     * @return the resolved PsiClass, or {@code null} if not a built-in component
     */
    @Nullable
    public static PsiClass resolveBuiltInComponent(@NotNull Project project, @NotNull String componentName) {
        String lowerName = componentName.toLowerCase();
        String className = BUILT_IN_COMPONENTS.get(lowerName);
        
        if (className != null) {
            String fqn = CORELIB_COMPONENTS_PACKAGE + "." + className;
            return JavaPsiFacade.getInstance(project).findClass(
                    fqn,
                    GlobalSearchScope.allScope(project)
            );
        }
        
        return null;
    }

    /**
     * Resolves a project-specific Tapestry component.
     *
     * <p>Component names can include subpackages using dot notation:
     * <ul>
     *     <li>{@code FeedbackComponent} - Component in the components package</li>
     *     <li>{@code ui.FeedbackComponent} - Component in components.ui package</li>
     * </ul>
     *
     * @param project the current project
     * @param componentName the component name (may include subpackage)
     * @return the resolved PsiClass, or {@code null} if not found
     */
    @Nullable
    public static PsiClass resolveProjectComponent(@NotNull Project project, @NotNull String componentName) {
        // Handle subpackage notation (e.g., ui.FeedbackComponent)
        String className;
        String subPackage = "";
        
        int lastDotIndex = componentName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            subPackage = componentName.substring(0, lastDotIndex).toLowerCase();
            className = componentName.substring(lastDotIndex + 1);
        } else {
            className = componentName;
        }

        // Search for Java files with this class name
        String javaFileName = className + ".java";
        Collection<com.intellij.openapi.vfs.VirtualFile> files = FilenameIndex.getVirtualFilesByName(
                javaFileName,
                GlobalSearchScope.projectScope(project)
        );

        PsiManager psiManager = PsiManager.getInstance(project);

        for (com.intellij.openapi.vfs.VirtualFile file : files) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof com.intellij.psi.PsiJavaFile javaFile) {
                for (PsiClass psiClass : javaFile.getClasses()) {
                    if (className.equals(psiClass.getName())) {
                        String qualifiedName = psiClass.getQualifiedName();
                        if (qualifiedName != null && isInComponentsPackage(qualifiedName, subPackage)) {
                            return psiClass;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Resolves a Tapestry mixin reference to its Java class.
     *
     * @param project the current project
     * @param mixinName the mixin name (e.g., "ui/confirmation", "Autocomplete")
     * @return the resolved PsiClass, or {@code null} if not found
     */
    @Nullable
    public static PsiClass resolveMixin(@NotNull Project project, @NotNull String mixinName) {
        if (mixinName.isEmpty()) {
            return null;
        }

        // Handle subpackage notation (using / as separator in TML)
        String className;
        String subPackage = "";

        // Mixins use / as path separator in TML (e.g., "ui/confirmation")
        int lastSlashIndex = mixinName.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            subPackage = mixinName.substring(0, lastSlashIndex).toLowerCase();
            className = mixinName.substring(lastSlashIndex + 1);
        } else {
            className = mixinName;
        }

        // Capitalize first letter for class name convention
        className = capitalizeFirst(className);

        // First try built-in mixins
        String builtInFqn = CORELIB_MIXINS_PACKAGE + "." + className;
        PsiClass builtInClass = JavaPsiFacade.getInstance(project).findClass(
                builtInFqn,
                GlobalSearchScope.allScope(project)
        );
        if (builtInClass != null) {
            return builtInClass;
        }

        // Search for project mixins
        String javaFileName = className + ".java";
        Collection<com.intellij.openapi.vfs.VirtualFile> files = FilenameIndex.getVirtualFilesByName(
                javaFileName,
                GlobalSearchScope.projectScope(project)
        );

        PsiManager psiManager = PsiManager.getInstance(project);

        for (com.intellij.openapi.vfs.VirtualFile file : files) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof com.intellij.psi.PsiJavaFile javaFile) {
                for (PsiClass psiClass : javaFile.getClasses()) {
                    if (className.equals(psiClass.getName())) {
                        String qualifiedName = psiClass.getQualifiedName();
                        if (qualifiedName != null && isInMixinsPackage(qualifiedName, subPackage)) {
                            return psiClass;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Extracts the component name from an XML tag.
     *
     * <p>Handles both tag-based and attribute-based component references:
     * <ul>
     *     <li>{@code <t:if>} - Returns "if"</li>
     *     <li>{@code <t:ui.Panel>} - Returns "ui.Panel"</li>
     *     <li>{@code <div t:type="layout">} - Returns "layout"</li>
     * </ul>
     *
     * @param tag the XML tag
     * @return the component name, or {@code null} if not a Tapestry component tag
     */
    @Nullable
    public static String getComponentName(@NotNull XmlTag tag) {
        String namespace = tag.getNamespace();
        String localName = tag.getLocalName();

        // Check if this is a t: prefixed tag
        if (TAPESTRY_NAMESPACE_URI.equals(namespace) || tag.getName().startsWith(TAPESTRY_NAMESPACE_PREFIX + ":")) {
            // The local name is the component name
            return localName;
        }

        // Check for t:type attribute on HTML tags
        XmlAttribute typeAttr = tag.getAttribute("type", TAPESTRY_NAMESPACE_URI);
        if (typeAttr == null) {
            typeAttr = tag.getAttribute("t:type");
        }
        if (typeAttr != null) {
            return typeAttr.getValue();
        }

        return null;
    }

    /**
     * Extracts mixin names from an XML tag's t:mixins attribute.
     *
     * @param tag the XML tag
     * @return list of mixin names, empty if no mixins
     */
    @NotNull
    public static List<String> getMixinNames(@NotNull XmlTag tag) {
        XmlAttribute mixinsAttr = tag.getAttribute("mixins", TAPESTRY_NAMESPACE_URI);
        if (mixinsAttr == null) {
            mixinsAttr = tag.getAttribute("t:mixins");
        }

        if (mixinsAttr == null || mixinsAttr.getValue() == null) {
            return Collections.emptyList();
        }

        // Mixins are comma-separated
        String[] mixins = mixinsAttr.getValue().split(",");
        List<String> result = new ArrayList<>();
        for (String mixin : mixins) {
            String trimmed = mixin.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Checks if the tag is a Tapestry component tag or has Tapestry attributes.
     *
     * @param tag the XML tag to check
     * @return {@code true} if this is a Tapestry component reference
     */
    public static boolean isTapestryComponentTag(@NotNull XmlTag tag) {
        return getComponentName(tag) != null;
    }

    /**
     * Checks if the tag is a Tapestry parameter tag (p: namespace).
     *
     * @param tag the XML tag to check
     * @return {@code true} if this is a parameter tag
     */
    public static boolean isTapestryParameterTag(@NotNull XmlTag tag) {
        String namespace = tag.getNamespace();
        return PARAMETER_NAMESPACE_URI.equals(namespace) || 
               tag.getName().startsWith(PARAMETER_NAMESPACE_PREFIX + ":");
    }

    /**
     * Gets the parameter name from a p: tag.
     *
     * @param tag the parameter tag
     * @return the parameter name, or {@code null} if not a parameter tag
     */
    @Nullable
    public static String getParameterName(@NotNull XmlTag tag) {
        if (isTapestryParameterTag(tag)) {
            return tag.getLocalName();
        }
        return null;
    }

    /**
     * Checks if a qualified name is in the components package with optional subpackage.
     */
    private static boolean isInComponentsPackage(@NotNull String qualifiedName, @NotNull String subPackage) {
        String packageName = Tapestry5Conventions.getPackageName(qualifiedName).toLowerCase();
        
        if (subPackage.isEmpty()) {
            // Direct component in components package
            return packageName.endsWith("." + Tapestry5Conventions.COMPONENTS_PACKAGE) ||
                   packageName.contains("." + Tapestry5Conventions.COMPONENTS_PACKAGE + ".");
        } else {
            // Component in a subpackage of components
            String expectedSuffix = "." + Tapestry5Conventions.COMPONENTS_PACKAGE + "." + subPackage;
            return packageName.endsWith(expectedSuffix) || packageName.contains(expectedSuffix + ".");
        }
    }

    /**
     * Checks if a qualified name is in the mixins package with optional subpackage.
     */
    private static boolean isInMixinsPackage(@NotNull String qualifiedName, @NotNull String subPackage) {
        String packageName = Tapestry5Conventions.getPackageName(qualifiedName).toLowerCase();

        if (subPackage.isEmpty()) {
            return packageName.endsWith("." + Tapestry5Conventions.MIXINS_PACKAGE) ||
                   packageName.contains("." + Tapestry5Conventions.MIXINS_PACKAGE + ".");
        } else {
            String expectedSuffix = "." + Tapestry5Conventions.MIXINS_PACKAGE + "." + subPackage;
            return packageName.endsWith(expectedSuffix) || packageName.contains(expectedSuffix + ".");
        }
    }

    /**
     * Capitalizes the first letter of a string.
     */
    private static String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
