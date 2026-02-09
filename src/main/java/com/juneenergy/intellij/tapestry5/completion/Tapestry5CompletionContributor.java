/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ProcessingContext;
import com.juneenergy.intellij.tapestry5.util.Tapestry5Conventions;
import com.juneenergy.intellij.tapestry5.util.Tapestry5FileUtils;
import com.juneenergy.intellij.tapestry5.util.TapestryComponentResolver;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

/**
 * Completion contributor for Tapestry 5 TML files.
 *
 * <p>Provides code completion for:
 * <ul>
 *     <li>Component names in t:* tags (e.g., {@code <t:if>}, {@code <t:loop>})</li>
 *     <li>Component type in t:type attributes</li>
 *     <li>Mixin names in t:mixins attributes</li>
 * </ul>
 *
 * @author June Energy
 * @since 1.0.0
 */
public class Tapestry5CompletionContributor extends CompletionContributor {

    /** Built-in Tapestry core component names for completion. */
    private static final Map<String, String> BUILT_IN_COMPONENTS = createBuiltInComponentsMap();

    /** Built-in Tapestry core mixin names for completion. */
    private static final Set<String> BUILT_IN_MIXINS = createBuiltInMixinsSet();

    public Tapestry5CompletionContributor() {
        // Register completion provider for t:type attribute values
        extend(CompletionType.BASIC,
                XmlPatterns.xmlAttributeValue()
                        .withParent(XmlPatterns.xmlAttribute().withName("t:type")),
                new TapestryTypeAttributeCompletionProvider());

        // Register completion provider for t:mixins attribute values
        extend(CompletionType.BASIC,
                XmlPatterns.xmlAttributeValue()
                        .withParent(XmlPatterns.xmlAttribute().withName("t:mixins")),
                new TapestryMixinsCompletionProvider());

        // Register completion provider for XML tag names in t: namespace
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(XmlToken.class)
                        .withElementType(XmlTokenType.XML_NAME),
                new TapestryTagNameCompletionProvider());
    }

    /**
     * Creates the map of built-in Tapestry component names with descriptions.
     */
    private static Map<String, String> createBuiltInComponentsMap() {
        Map<String, String> map = new LinkedHashMap<>();

        // Control flow components
        map.put("if", "Conditionally renders content based on a test parameter");
        map.put("unless", "Renders content when condition is false (inverse of If)");
        map.put("delegate", "Delegates rendering to another Block");
        map.put("block", "A named block of renderable content");
        map.put("body", "Renders the body of the enclosing component");
        map.put("renderobject", "Renders an object using its natural renderer");

        // Loop components
        map.put("loop", "Iterates over a collection rendering content for each item");
        map.put("grid", "Displays data in a tabular format with sorting and paging");
        map.put("gridrows", "Renders the rows of a Grid");
        map.put("gridcolumns", "Renders the column headers of a Grid");
        map.put("gridcell", "Renders a single cell in a Grid");
        map.put("gridpager", "Provides pagination controls for a Grid");

        // Form components
        map.put("form", "A form container for input components");
        map.put("textfield", "Single-line text input field");
        map.put("textarea", "Multi-line text input area");
        map.put("passwordfield", "Password input field (masked)");
        map.put("checkbox", "Boolean checkbox input");
        map.put("radio", "Radio button input");
        map.put("radiogroup", "Group of radio buttons");
        map.put("select", "Dropdown selection list");
        map.put("submit", "Form submit button");
        map.put("hidden", "Hidden form field");
        map.put("label", "Label for a form field");
        map.put("errors", "Displays form validation errors");
        map.put("datefield", "Date input field with calendar popup");
        map.put("upload", "File upload field");
        map.put("palette", "Dual-list selection component");
        map.put("checklist", "Multi-selection checkbox list");

        // Link components
        map.put("actionlink", "Link that triggers an action on the server");
        map.put("eventlink", "Link that triggers a component event");
        map.put("pagelink", "Link to another Tapestry page");
        map.put("externallink", "Link to an external URL");

        // Layout components
        map.put("zone", "Ajax-updateable region");
        map.put("any", "Generic element that can render as any HTML tag");
        map.put("output", "Outputs a value with proper escaping");
        map.put("outputraw", "Outputs a value without HTML escaping");
        map.put("beandisplay", "Displays bean properties in read-only format");
        map.put("beaneditform", "Form for editing bean properties");
        map.put("beaneditor", "Editor component for bean properties");
        map.put("propertyeditor", "Editor for a single property");
        map.put("propertydisplay", "Display for a single property");

        // Alert components
        map.put("alerts", "Displays flash/alert messages");

        // Ajax components
        map.put("ajaxformloop", "Loop that supports adding/removing rows via Ajax");
        map.put("addrowlink", "Link to add a row in AjaxFormLoop");
        map.put("removerowlink", "Link to remove a row in AjaxFormLoop");
        map.put("formfragment", "Conditionally shown/hidden form fragment");
        map.put("trigger", "Triggers an event");
        map.put("progressivedisplay", "Lazily loads content via Ajax");
        map.put("kicker", "Kicks off client-side initialization");

        // Other components
        map.put("tree", "Hierarchical tree display component");
        map.put("dynamic", "Dynamically renders a template");

        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates the set of built-in Tapestry mixin names.
     */
    private static Set<String> createBuiltInMixinsSet() {
        Set<String> set = new LinkedHashSet<>();
        set.add("Autocomplete");
        set.add("DiscardBody");
        set.add("FormFieldFocus");
        set.add("NotEmpty");
        set.add("RenderClientId");
        set.add("RenderDisabled");
        set.add("RenderInformals");
        set.add("RenderNotification");
        set.add("TriggerFragment");
        set.add("ZoneRefresh");
        return Collections.unmodifiableSet(set);
    }

    /**
     * Completion provider for t:type attribute values.
     */
    private static class TapestryTypeAttributeCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            PsiElement element = parameters.getPosition();

            // Check if we're in a TML file
            if (!Tapestry5FileUtils.isTmlFile(element.getContainingFile())) {
                return;
            }

            Project project = element.getProject();

            // Add built-in components
            for (Map.Entry<String, String> entry : BUILT_IN_COMPONENTS.entrySet()) {
                result.addElement(createComponentLookupElement(entry.getKey(), entry.getValue(), true));
            }

            // Add project components
            addProjectComponents(project, result);
        }
    }

    /**
     * Completion provider for t:mixins attribute values.
     */
    private static class TapestryMixinsCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            PsiElement element = parameters.getPosition();

            // Check if we're in a TML file
            if (!Tapestry5FileUtils.isTmlFile(element.getContainingFile())) {
                return;
            }

            Project project = element.getProject();

            // Add built-in mixins
            for (String mixin : BUILT_IN_MIXINS) {
                result.addElement(createMixinLookupElement(mixin, true));
            }

            // Add project mixins
            addProjectMixins(project, result);
        }
    }

    /**
     * Completion provider for Tapestry tag names (t:componentName).
     */
    private static class TapestryTagNameCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            PsiElement element = parameters.getPosition();

            // Check if we're in a TML file
            if (!Tapestry5FileUtils.isTmlFile(element.getContainingFile())) {
                return;
            }

            // Check if we're typing after "t:"
            PsiElement parent = element.getParent();
            if (!(parent instanceof XmlTag xmlTag)) {
                return;
            }

            String tagName = xmlTag.getName();
            String prefix = parameters.getPosition().getText();

            // Only provide completions when typing t: prefixed tags
            if (!tagName.startsWith("t:") && !prefix.startsWith("t:")) {
                return;
            }

            Project project = element.getProject();

            // Add built-in components with t: prefix
            for (Map.Entry<String, String> entry : BUILT_IN_COMPONENTS.entrySet()) {
                String componentName = "t:" + entry.getKey();
                result.addElement(createTagLookupElement(componentName, entry.getValue(), true));
            }

            // Add project components with t: prefix
            addProjectComponentsAsTag(project, result);
        }
    }

    /**
     * Adds project-specific components to the completion result.
     */
    private static void addProjectComponents(@NotNull Project project,
                                             @NotNull CompletionResultSet result) {
        // Search for Java files in components packages
        Collection<VirtualFile> javaFiles = FilenameIndex.getAllFilesByExt(
                project,
                "java",
                GlobalSearchScope.projectScope(project)
        );

        PsiManager psiManager = PsiManager.getInstance(project);

        for (VirtualFile file : javaFiles) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof PsiJavaFile javaFile) {
                for (PsiClass psiClass : javaFile.getClasses()) {
                    String qualifiedName = psiClass.getQualifiedName();
                    if (qualifiedName != null && Tapestry5Conventions.isInComponentsPackage(qualifiedName)) {
                        String componentName = getComponentNameFromClass(psiClass, qualifiedName);
                        if (componentName != null) {
                            String typeText = psiClass.getName();
                            result.addElement(createComponentLookupElement(componentName, "Project component: " + typeText, false));
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds project-specific components as tag completions.
     */
    private static void addProjectComponentsAsTag(@NotNull Project project,
                                                  @NotNull CompletionResultSet result) {
        Collection<VirtualFile> javaFiles = FilenameIndex.getAllFilesByExt(
                project,
                "java",
                GlobalSearchScope.projectScope(project)
        );

        PsiManager psiManager = PsiManager.getInstance(project);

        for (VirtualFile file : javaFiles) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof PsiJavaFile javaFile) {
                for (PsiClass psiClass : javaFile.getClasses()) {
                    String qualifiedName = psiClass.getQualifiedName();
                    if (qualifiedName != null && Tapestry5Conventions.isInComponentsPackage(qualifiedName)) {
                        String componentName = getComponentNameFromClass(psiClass, qualifiedName);
                        if (componentName != null) {
                            String tagName = "t:" + componentName;
                            String typeText = psiClass.getName();
                            result.addElement(createTagLookupElement(tagName, "Project component: " + typeText, false));
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds project-specific mixins to the completion result.
     */
    private static void addProjectMixins(@NotNull Project project,
                                         @NotNull CompletionResultSet result) {
        Collection<VirtualFile> javaFiles = FilenameIndex.getAllFilesByExt(
                project,
                "java",
                GlobalSearchScope.projectScope(project)
        );

        PsiManager psiManager = PsiManager.getInstance(project);

        for (VirtualFile file : javaFiles) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof PsiJavaFile javaFile) {
                for (PsiClass psiClass : javaFile.getClasses()) {
                    String qualifiedName = psiClass.getQualifiedName();
                    if (qualifiedName != null && Tapestry5Conventions.isInMixinsPackage(qualifiedName)) {
                        String mixinName = getMixinNameFromClass(psiClass, qualifiedName);
                        if (mixinName != null) {
                            result.addElement(createMixinLookupElement(mixinName, false));
                        }
                    }
                }
            }
        }
    }

    /**
     * Extracts the component name from a class, including subpackage prefix if needed.
     */
    private static String getComponentNameFromClass(@NotNull PsiClass psiClass, @NotNull String qualifiedName) {
        String className = psiClass.getName();
        if (className == null) {
            return null;
        }

        // Find the position of ".components." in the package name
        String packageName = Tapestry5Conventions.getPackageName(qualifiedName);
        int componentsIndex = packageName.indexOf("." + Tapestry5Conventions.COMPONENTS_PACKAGE + ".");

        if (componentsIndex >= 0) {
            // Extract subpackage after "components."
            String subPackage = packageName.substring(
                    componentsIndex + Tapestry5Conventions.COMPONENTS_PACKAGE.length() + 2
            );
            if (!subPackage.isEmpty()) {
                // Convert package separators to dots (e.g., ui.FeedbackComponent)
                return subPackage.replace('.', '.') + "." + className;
            }
        }

        return className;
    }

    /**
     * Extracts the mixin name from a class, including subpackage prefix if needed.
     */
    private static String getMixinNameFromClass(@NotNull PsiClass psiClass, @NotNull String qualifiedName) {
        String className = psiClass.getName();
        if (className == null) {
            return null;
        }

        // Find the position of ".mixins." in the package name
        String packageName = Tapestry5Conventions.getPackageName(qualifiedName);
        int mixinsIndex = packageName.indexOf("." + Tapestry5Conventions.MIXINS_PACKAGE + ".");

        if (mixinsIndex >= 0) {
            // Extract subpackage after "mixins."
            String subPackage = packageName.substring(
                    mixinsIndex + Tapestry5Conventions.MIXINS_PACKAGE.length() + 2
            );
            if (!subPackage.isEmpty()) {
                // Convert package separators to forward slashes for mixin notation
                return subPackage.replace('.', '/') + "/" + className;
            }
        }

        return className;
    }

    /**
     * Creates a lookup element for a component.
     */
    private static LookupElement createComponentLookupElement(@NotNull String name,
                                                              @NotNull String description,
                                                              boolean isBuiltIn) {
        Icon icon = isBuiltIn ? AllIcons.Nodes.Plugin : AllIcons.Nodes.Class;
        String typeText = isBuiltIn ? "Tapestry Core" : "Project";

        return LookupElementBuilder.create(name)
                .withIcon(icon)
                .withTypeText(typeText, true)
                .withTailText(" " + description, true)
                .withCaseSensitivity(false);
    }

    /**
     * Creates a lookup element for a mixin.
     */
    private static LookupElement createMixinLookupElement(@NotNull String name, boolean isBuiltIn) {
        Icon icon = isBuiltIn ? AllIcons.Nodes.Plugin : AllIcons.Nodes.Class;
        String typeText = isBuiltIn ? "Tapestry Mixin" : "Project Mixin";

        return LookupElementBuilder.create(name)
                .withIcon(icon)
                .withTypeText(typeText, true)
                .withCaseSensitivity(false);
    }

    /**
     * Creates a lookup element for a tag completion.
     */
    private static LookupElement createTagLookupElement(@NotNull String tagName,
                                                        @NotNull String description,
                                                        boolean isBuiltIn) {
        Icon icon = isBuiltIn ? AllIcons.Nodes.Plugin : AllIcons.Nodes.Class;
        String typeText = isBuiltIn ? "Tapestry Core" : "Project";

        return LookupElementBuilder.create(tagName)
                .withIcon(icon)
                .withTypeText(typeText, true)
                .withTailText(" " + description, true)
                .withCaseSensitivity(false)
                .withInsertHandler((insertContext, item) -> {
                    // Add closing tag after cursor
                    int tailOffset = insertContext.getTailOffset();
                    insertContext.getDocument().insertString(tailOffset, "></" + tagName + ">");
                    insertContext.getEditor().getCaretModel().moveToOffset(tailOffset + 1);
                });
    }
}
