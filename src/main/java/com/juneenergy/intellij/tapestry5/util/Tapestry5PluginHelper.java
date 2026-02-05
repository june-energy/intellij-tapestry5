/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Helper utility class for common plugin operations.
 *
 * <p>Provides methods for:
 * <ul>
 *     <li>File search and navigation</li>
 *     <li>User notifications</li>
 *     <li>PSI utilities for Java introspection</li>
 * </ul>
 *
 * @author June Energy
 * @since 1.0.0
 */
public final class Tapestry5PluginHelper {

    /** The notification group ID as defined in plugin.xml. */
    public static final String NOTIFICATION_GROUP_ID = "Tapestry5Notifications";

    private Tapestry5PluginHelper() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // File Navigation
    // ========================================

    /**
     * Opens a file in the editor.
     *
     * @param project the current project
     * @param file the file to open
     */
    public static void openFile(@NotNull Project project, @NotNull VirtualFile file) {
        FileEditorManager.getInstance(project).openFile(file, true);
    }

    /**
     * Opens a PSI file in the editor.
     *
     * @param project the current project
     * @param psiFile the PSI file to open
     */
    public static void openFile(@NotNull Project project, @NotNull PsiFile psiFile) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile != null) {
            openFile(project, virtualFile);
        }
    }

    /**
     * Gets the currently open file in the editor.
     *
     * @param project the current project
     * @return the currently open file, or {@code null} if no file is open
     */
    @Nullable
    public static VirtualFile getCurrentFile(@NotNull Project project) {
        VirtualFile[] files = FileEditorManager.getInstance(project).getSelectedFiles();
        return files.length > 0 ? files[0] : null;
    }

    // ========================================
    // File Search
    // ========================================

    /**
     * Finds files by name in the project.
     *
     * @param project the current project
     * @param fileName the file name to search for
     * @return collection of matching files
     */
    @NotNull
    public static Collection<VirtualFile> findFilesByName(@NotNull Project project, @NotNull String fileName) {
        return FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.projectScope(project));
    }

    /**
     * Finds a TML template file for a given Java class.
     *
     * <p>The template is expected to be:
     * <ul>
     *     <li>In the same package as the Java class</li>
     *     <li>Named the same as the class with .tml extension</li>
     * </ul>
     *
     * @param project the current project
     * @param javaClass the Java class to find the template for
     * @return the TML file, or {@code null} if not found
     */
    @Nullable
    public static VirtualFile findTmlForClass(@NotNull Project project, @NotNull PsiClass javaClass) {
        String templateName = Tapestry5FileUtils.getTmlFileNameForClass(javaClass);
        Collection<VirtualFile> files = findFilesByName(project, templateName);

        if (files.isEmpty()) {
            return null;
        }

        // If there's only one file, return it
        if (files.size() == 1) {
            return files.iterator().next();
        }

        // Multiple files found - try to find the one in the same package
        String packageName = Tapestry5Conventions.getPackageName(javaClass.getQualifiedName());
        String expectedPath = packageName.replace('.', '/') + "/" + templateName;

        return files.stream()
                .filter(f -> f.getPath().endsWith(expectedPath))
                .findFirst()
                .orElse(files.iterator().next()); // Fall back to first match
    }

    /**
     * Finds the Java class for a given TML template file.
     *
     * @param project the current project
     * @param tmlFile the TML template file
     * @return the Java class, or {@code null} if not found
     */
    @Nullable
    public static PsiClass findClassForTml(@NotNull Project project, @NotNull VirtualFile tmlFile) {
        String className = Tapestry5FileUtils.getClassNameForTmlFile(tmlFile);
        if (className == null) {
            return null;
        }

        // Get the package path from the TML file location
        String packagePath = getPackagePathFromFile(tmlFile);

        // Search for Java files with the same name
        String javaFileName = className + "." + Tapestry5FileUtils.JAVA_EXTENSION;
        Collection<VirtualFile> javaFiles = findFilesByName(project, javaFileName);

        if (javaFiles.isEmpty()) {
            return null;
        }

        PsiManager psiManager = PsiManager.getInstance(project);

        // Try to find a matching class in the same package structure
        for (VirtualFile javaFile : javaFiles) {
            PsiFile psiFile = psiManager.findFile(javaFile);
            if (psiFile instanceof PsiJavaFile javaFileImpl) {
                PsiClass[] classes = javaFileImpl.getClasses();
                for (PsiClass psiClass : classes) {
                    if (className.equals(psiClass.getName())) {
                        // Check if the package matches
                        String qualifiedName = psiClass.getQualifiedName();
                        if (qualifiedName != null && matchesPackagePath(qualifiedName, packagePath)) {
                            return psiClass;
                        }
                    }
                }
            }
        }

        // If no exact match, return the first class with matching name
        for (VirtualFile javaFile : javaFiles) {
            PsiFile psiFile = psiManager.findFile(javaFile);
            if (psiFile instanceof PsiJavaFile javaFileImpl) {
                PsiClass[] classes = javaFileImpl.getClasses();
                for (PsiClass psiClass : classes) {
                    if (className.equals(psiClass.getName())) {
                        return psiClass;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds a PSI class by its fully qualified name.
     *
     * @param project the current project
     * @param qualifiedName the fully qualified class name
     * @return the PSI class, or {@code null} if not found
     */
    @Nullable
    public static PsiClass findClass(@NotNull Project project, @NotNull String qualifiedName) {
        return JavaPsiFacade.getInstance(project).findClass(
                qualifiedName,
                GlobalSearchScope.projectScope(project)
        );
    }

    // ========================================
    // Notifications
    // ========================================

    /**
     * Shows an information notification.
     *
     * @param project the current project
     * @param content the notification content
     */
    public static void showInfo(@NotNull Project project, @NotNull String content) {
        showNotification(project, content, NotificationType.INFORMATION);
    }

    /**
     * Shows a warning notification.
     *
     * @param project the current project
     * @param content the notification content
     */
    public static void showWarning(@NotNull Project project, @NotNull String content) {
        showNotification(project, content, NotificationType.WARNING);
    }

    /**
     * Shows an error notification.
     *
     * @param project the current project
     * @param content the notification content
     */
    public static void showError(@NotNull Project project, @NotNull String content) {
        showNotification(project, content, NotificationType.ERROR);
    }

    /**
     * Shows a notification with the specified type.
     *
     * @param project the current project
     * @param content the notification content
     * @param type the notification type
     */
    public static void showNotification(@NotNull Project project, @NotNull String content,
                                        @NotNull NotificationType type) {
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification(content, type);
        notification.notify(project);
    }

    // ========================================
    // PSI Utilities
    // ========================================

    /**
     * Gets the PSI file from a virtual file.
     *
     * @param project the current project
     * @param file the virtual file
     * @return the PSI file, or {@code null} if not available
     */
    @Nullable
    public static PsiFile getPsiFile(@NotNull Project project, @NotNull VirtualFile file) {
        return PsiManager.getInstance(project).findFile(file);
    }

    /**
     * Gets the main class from a Java file.
     *
     * @param javaFile the Java PSI file
     * @return the main class (first public class), or {@code null} if not found
     */
    @Nullable
    public static PsiClass getMainClass(@NotNull PsiJavaFile javaFile) {
        PsiClass[] classes = javaFile.getClasses();
        if (classes.length == 0) {
            return null;
        }

        // Find the public class (typically the main class)
        String fileName = javaFile.getName();
        String expectedClassName = fileName.substring(0, fileName.lastIndexOf('.'));

        return Arrays.stream(classes)
                .filter(c -> expectedClassName.equals(c.getName()))
                .findFirst()
                .orElse(classes[0]);
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * Extracts the package path from a file's location.
     *
     * @param file the file
     * @return the package-like path (e.g., "com/example/pages")
     */
    @NotNull
    private static String getPackagePathFromFile(@NotNull VirtualFile file) {
        String path = file.getPath();

        // Find common source roots and extract the package path
        String[] sourceRoots = {
                "/src/main/resources/",
                "/src/main/java/",
                "/src/test/resources/",
                "/src/test/java/"
        };

        for (String root : sourceRoots) {
            int index = path.indexOf(root);
            if (index >= 0) {
                String packagePath = path.substring(index + root.length());
                // Remove the file name to get just the package path
                int lastSlash = packagePath.lastIndexOf('/');
                return lastSlash > 0 ? packagePath.substring(0, lastSlash) : "";
            }
        }

        // Fall back to parent directory path
        VirtualFile parent = file.getParent();
        return parent != null ? parent.getPath() : "";
    }

    /**
     * Checks if a qualified class name matches a package path.
     *
     * @param qualifiedName the fully qualified class name
     * @param packagePath the package path (using / as separator)
     * @return {@code true} if they match
     */
    private static boolean matchesPackagePath(@NotNull String qualifiedName, @NotNull String packagePath) {
        String packageFromQualified = Tapestry5Conventions.getPackageName(qualifiedName)
                .replace('.', '/');
        return packagePath.endsWith(packageFromQualified) ||
               packageFromQualified.endsWith(packagePath);
    }
}
