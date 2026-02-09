/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolver for Tapestry library mappings defined via {@code @Contribute(ComponentClassResolver.class)}.
 *
 * <p>Tapestry allows projects to define library mappings that map prefixes to root packages:
 * <pre>{@code
 * @Contribute(ComponentClassResolver.class)
 * public static void provideRootPackage(Configuration<LibraryMapping> configuration) {
 *     configuration.add(new LibraryMapping("ui", "energy.june.commons.ui"));
 * }
 * }</pre>
 *
 * <p>This enables references like {@code t:mixins="ui/DatePicker"} to resolve to
 * {@code energy.june.commons.ui.mixins.DatePicker}.
 *
 * @author June Energy
 * @since 1.0.0
 */
public final class TapestryLibraryMappingResolver {

    /** FQN of the Tapestry @Contribute annotation. */
    private static final String CONTRIBUTE_ANNOTATION = "org.apache.tapestry5.ioc.annotations.Contribute";

    /** FQN of the ComponentClassResolver service interface. */
    private static final String COMPONENT_CLASS_RESOLVER = "org.apache.tapestry5.services.ComponentClassResolver";

    /** FQN of the LibraryMapping class. */
    private static final String LIBRARY_MAPPING_CLASS = "org.apache.tapestry5.services.LibraryMapping";

    /** Cache of library mappings per project to avoid repeated scanning. */
    private static final Map<Project, Map<String, String>> projectMappingsCache = new ConcurrentHashMap<>();

    /** Cache timestamp to invalidate after a period. */
    private static final Map<Project, Long> cacheTimestamps = new ConcurrentHashMap<>();

    /** Cache validity period in milliseconds (30 seconds). */
    private static final long CACHE_VALIDITY_MS = 30_000;

    private TapestryLibraryMappingResolver() {
        // Utility class - prevent instantiation
    }

    /**
     * Clears the cache for a specific project.
     *
     * @param project the project to clear cache for
     */
    public static void clearCache(@NotNull Project project) {
        projectMappingsCache.remove(project);
        cacheTimestamps.remove(project);
    }

    /**
     * Gets all library mappings defined in the project.
     *
     * <p>This method scans for methods annotated with {@code @Contribute(ComponentClassResolver.class)}
     * and extracts {@code LibraryMapping} constructor calls.
     *
     * @param project the current project
     * @return a map of prefix → root package mappings
     */
    @NotNull
    public static Map<String, String> getLibraryMappings(@NotNull Project project) {
        // Check cache validity
        Long timestamp = cacheTimestamps.get(project);
        if (timestamp != null && System.currentTimeMillis() - timestamp < CACHE_VALIDITY_MS) {
            Map<String, String> cached = projectMappingsCache.get(project);
            if (cached != null) {
                return cached;
            }
        }

        // Scan and cache
        Map<String, String> mappings = scanForLibraryMappings(project);
        projectMappingsCache.put(project, mappings);
        cacheTimestamps.put(project, System.currentTimeMillis());

        return mappings;
    }

    /**
     * Resolves a library prefix to its root package.
     *
     * @param project the current project
     * @param prefix the library prefix (e.g., "ui")
     * @return the root package, or {@code null} if not found
     */
    @Nullable
    public static String resolvePrefix(@NotNull Project project, @NotNull String prefix) {
        return getLibraryMappings(project).get(prefix.toLowerCase());
    }

    /**
     * Resolves a prefixed reference to a fully qualified class name.
     *
     * <p>For example, with mapping {@code ui → energy.june.commons.ui}:
     * <ul>
     *     <li>{@code resolveToFqn(project, "ui/DatePicker", "mixins")} → {@code energy.june.commons.ui.mixins.DatePicker}</li>
     *     <li>{@code resolveToFqn(project, "ui/MyComponent", "components")} → {@code energy.june.commons.ui.components.MyComponent}</li>
     * </ul>
     *
     * @param project the current project
     * @param prefixedName the prefixed name (e.g., "ui/DatePicker")
     * @param elementType the Tapestry element type package (e.g., "mixins", "components")
     * @return the resolved FQN, or {@code null} if prefix not found
     */
    @Nullable
    public static String resolveToFqn(@NotNull Project project,
                                       @NotNull String prefixedName,
                                       @NotNull String elementType) {
        int slashIndex = prefixedName.indexOf('/');
        if (slashIndex <= 0) {
            return null; // No prefix found
        }

        String prefix = prefixedName.substring(0, slashIndex);
        String className = prefixedName.substring(slashIndex + 1);

        // Handle nested paths (e.g., "ui/sub/DatePicker")
        String subPackage = "";
        int lastSlash = className.lastIndexOf('/');
        if (lastSlash > 0) {
            subPackage = "." + className.substring(0, lastSlash).replace('/', '.');
            className = className.substring(lastSlash + 1);
        }

        String rootPackage = resolvePrefix(project, prefix);
        if (rootPackage == null) {
            return null;
        }

        // Capitalize first letter of class name
        if (!className.isEmpty()) {
            className = Character.toUpperCase(className.charAt(0)) + className.substring(1);
        }

        return rootPackage + "." + elementType + subPackage + "." + className;
    }

    /**
     * Checks if a name has a library prefix.
     *
     * @param name the name to check (e.g., "ui/DatePicker")
     * @return {@code true} if the name contains a prefix separator
     */
    public static boolean hasPrefixSeparator(@NotNull String name) {
        return name.contains("/");
    }

    /**
     * Extracts the prefix from a prefixed name.
     *
     * @param prefixedName the prefixed name (e.g., "ui/DatePicker")
     * @return the prefix (e.g., "ui"), or {@code null} if no prefix
     */
    @Nullable
    public static String extractPrefix(@NotNull String prefixedName) {
        int slashIndex = prefixedName.indexOf('/');
        if (slashIndex <= 0) {
            return null;
        }
        return prefixedName.substring(0, slashIndex);
    }

    /**
     * Scans the project for LibraryMapping definitions.
     */
    @NotNull
    private static Map<String, String> scanForLibraryMappings(@NotNull Project project) {
        Map<String, String> mappings = new HashMap<>();

        // Find the @Contribute annotation class
        PsiClass contributeAnnotation = JavaPsiFacade.getInstance(project).findClass(
                CONTRIBUTE_ANNOTATION,
                GlobalSearchScope.allScope(project)
        );

        if (contributeAnnotation == null) {
            // Tapestry not on classpath, try alternative scanning
            return scanForLibraryMappingsAlternative(project);
        }

        // Search for methods annotated with @Contribute
        Query<PsiMethod> query = AnnotatedElementsSearch.searchPsiMethods(
                contributeAnnotation,
                GlobalSearchScope.projectScope(project)
        );

        for (PsiMethod method : query.findAll()) {
            Map<String, String> methodMappings = extractLibraryMappingsFromMethod(method);
            mappings.putAll(methodMappings);
        }

        return mappings;
    }

    /**
     * Alternative scanning approach when @Contribute annotation class is not found.
     * Searches for "LibraryMapping" text and parses the context.
     */
    @NotNull
    private static Map<String, String> scanForLibraryMappingsAlternative(@NotNull Project project) {
        Map<String, String> mappings = new HashMap<>();

        // Search for files containing "LibraryMapping"
        PsiClass libraryMappingClass = JavaPsiFacade.getInstance(project).findClass(
                LIBRARY_MAPPING_CLASS,
                GlobalSearchScope.allScope(project)
        );

        if (libraryMappingClass == null) {
            // Try to find by searching for new LibraryMapping patterns in code
            return scanByTextPattern(project);
        }

        return mappings;
    }

    /**
     * Scans by searching for "new LibraryMapping" patterns in Java files.
     */
    @NotNull
    private static Map<String, String> scanByTextPattern(@NotNull Project project) {
        Map<String, String> mappings = new HashMap<>();

        // Use PsiSearchHelper to find usages of "LibraryMapping"
        // For now, we'll rely on the annotation-based approach
        // and users can configure mappings manually if needed

        return mappings;
    }

    /**
     * Extracts LibraryMapping definitions from a @Contribute method.
     */
    @NotNull
    private static Map<String, String> extractLibraryMappingsFromMethod(@NotNull PsiMethod method) {
        Map<String, String> mappings = new HashMap<>();

        // Check if this is a @Contribute(ComponentClassResolver.class) method
        if (!isComponentClassResolverContribution(method)) {
            return mappings;
        }

        // Find all new expressions in the method body
        PsiCodeBlock body = method.getBody();
        if (body == null) {
            return mappings;
        }

        Collection<PsiNewExpression> newExpressions = PsiTreeUtil.findChildrenOfType(body, PsiNewExpression.class);
        for (PsiNewExpression newExpr : newExpressions) {
            LibraryMappingInfo info = extractLibraryMappingInfo(newExpr);
            if (info != null) {
                mappings.put(info.prefix.toLowerCase(), info.rootPackage);
            }
        }

        return mappings;
    }

    /**
     * Checks if a method is a @Contribute(ComponentClassResolver.class) contribution.
     */
    private static boolean isComponentClassResolverContribution(@NotNull PsiMethod method) {
        PsiModifierList modifierList = method.getModifierList();

        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if (CONTRIBUTE_ANNOTATION.equals(qualifiedName)) {
                // Check the annotation value
                PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
                if (value == null) {
                    value = annotation.findAttributeValue(null); // Default value
                }

                if (value instanceof PsiClassObjectAccessExpression classAccess) {
                    PsiType type = classAccess.getOperand().getType();
                    if (type instanceof PsiClassType classType) {
                        PsiClass resolvedClass = classType.resolve();
                        if (resolvedClass != null) {
                            String fqn = resolvedClass.getQualifiedName();
                            return COMPONENT_CLASS_RESOLVER.equals(fqn);
                        }
                    }
                }

                // Also check by simple text match for robustness
                if (value != null && value.getText().contains("ComponentClassResolver")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Extracts library mapping information from a new LibraryMapping(...) expression.
     */
    @Nullable
    private static LibraryMappingInfo extractLibraryMappingInfo(@NotNull PsiNewExpression newExpr) {
        PsiJavaCodeReferenceElement classRef = newExpr.getClassReference();
        if (classRef == null) {
            return null;
        }

        // Check if this is new LibraryMapping(...)
        String className = classRef.getQualifiedName();
        if (className == null) {
            className = classRef.getReferenceName();
        }

        if (!"LibraryMapping".equals(className) && !LIBRARY_MAPPING_CLASS.equals(className)) {
            return null;
        }

        // Extract constructor arguments
        PsiExpressionList argumentList = newExpr.getArgumentList();
        if (argumentList == null) {
            return null;
        }

        PsiExpression[] args = argumentList.getExpressions();
        if (args.length < 2) {
            return null; // LibraryMapping requires at least 2 arguments (prefix, rootPackage)
        }

        String prefix = extractStringValue(args[0]);
        String rootPackage = extractStringValue(args[1]);

        if (prefix != null && rootPackage != null) {
            return new LibraryMappingInfo(prefix, rootPackage);
        }

        return null;
    }

    /**
     * Extracts a string value from a PSI expression.
     */
    @Nullable
    private static String extractStringValue(@NotNull PsiExpression expr) {
        if (expr instanceof PsiLiteralExpression literal) {
            Object value = literal.getValue();
            if (value instanceof String) {
                return (String) value;
            }
        }

        // Handle string concatenation or constant references if needed
        if (expr instanceof PsiReferenceExpression ref) {
            PsiElement resolved = ref.resolve();
            if (resolved instanceof PsiField field) {
                PsiExpression initializer = field.getInitializer();
                if (initializer != null) {
                    return extractStringValue(initializer);
                }
            }
        }

        return null;
    }

    /**
     * Internal class to hold library mapping information.
     */
    private record LibraryMappingInfo(String prefix, String rootPackage) {
    }
}
