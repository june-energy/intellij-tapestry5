/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for Tapestry 5 file type detection and manipulation.
 *
 * <p>Provides methods to:
 * <ul>
 *     <li>Detect TML (Tapestry Markup Language) template files</li>
 *     <li>Detect Java page/component files</li>
 *     <li>Check file location conventions (pages/, components/, mixins/)</li>
 * </ul>
 *
 * <h2>Tapestry 5 File Conventions</h2>
 * <ul>
 *     <li>Template files use the {@code .tml} extension</li>
 *     <li>Templates have the same name as their Java class</li>
 *     <li>Templates are located in the same package as their Java class</li>
 * </ul>
 *
 * @author June Energy
 * @since 1.0.0
 */
public final class Tapestry5FileUtils {

    /** The file extension for Tapestry Markup Language templates. */
    public static final String TML_EXTENSION = "tml";

    /** The file extension for Java files. */
    public static final String JAVA_EXTENSION = "java";

    private Tapestry5FileUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Checks if the given file is a TML template file.
     *
     * @param file the file to check
     * @return {@code true} if the file has a .tml extension, {@code false} otherwise
     */
    public static boolean isTmlFile(@Nullable VirtualFile file) {
        if (file == null || file.isDirectory()) {
            return false;
        }
        return TML_EXTENSION.equalsIgnoreCase(file.getExtension());
    }

    /**
     * Checks if the given PSI file is a TML template file.
     *
     * @param file the PSI file to check
     * @return {@code true} if the file has a .tml extension, {@code false} otherwise
     */
    public static boolean isTmlFile(@Nullable PsiFile file) {
        if (file == null) {
            return false;
        }
        return isTmlFile(file.getVirtualFile());
    }

    /**
     * Checks if the given file is a Java file.
     *
     * @param file the file to check
     * @return {@code true} if the file has a .java extension, {@code false} otherwise
     */
    public static boolean isJavaFile(@Nullable VirtualFile file) {
        if (file == null || file.isDirectory()) {
            return false;
        }
        return JAVA_EXTENSION.equalsIgnoreCase(file.getExtension());
    }

    /**
     * Checks if the given PSI file is a Java file.
     *
     * @param file the PSI file to check
     * @return {@code true} if the file is a Java file, {@code false} otherwise
     */
    public static boolean isJavaFile(@Nullable PsiFile file) {
        return file instanceof PsiJavaFile;
    }

    /**
     * Gets the file name without extension.
     *
     * @param file the file
     * @return the file name without extension, or {@code null} if file is null
     */
    @Nullable
    public static String getFileNameWithoutExtension(@Nullable VirtualFile file) {
        if (file == null) {
            return null;
        }
        return file.getNameWithoutExtension();
    }

    /**
     * Gets the file name without extension from a PSI file.
     *
     * @param file the PSI file
     * @return the file name without extension, or {@code null} if file is null
     */
    @Nullable
    public static String getFileNameWithoutExtension(@Nullable PsiFile file) {
        if (file == null) {
            return null;
        }
        return getFileNameWithoutExtension(file.getVirtualFile());
    }

    /**
     * Gets the expected TML template file name for a Java class.
     *
     * @param javaClass the Java class
     * @return the expected template file name (e.g., "Index.tml" for Index.java)
     */
    @NotNull
    public static String getTmlFileNameForClass(@NotNull PsiClass javaClass) {
        return javaClass.getName() + "." + TML_EXTENSION;
    }

    /**
     * Gets the expected Java class name for a TML template file.
     *
     * @param tmlFile the TML template file
     * @return the expected Java class name (e.g., "Index" for Index.tml)
     */
    @Nullable
    public static String getClassNameForTmlFile(@NotNull VirtualFile tmlFile) {
        return tmlFile.getNameWithoutExtension();
    }

    /**
     * Checks if the given file is in the resources directory.
     *
     * @param file the file to check
     * @return {@code true} if the file is under a resources directory
     */
    public static boolean isInResourcesDirectory(@Nullable VirtualFile file) {
        if (file == null) {
            return false;
        }
        String path = file.getPath();
        return path.contains("/resources/") || path.contains("\\resources\\");
    }

    /**
     * Checks if the given file is in the source (java) directory.
     *
     * @param file the file to check
     * @return {@code true} if the file is under a java source directory
     */
    public static boolean isInSourceDirectory(@Nullable VirtualFile file) {
        if (file == null) {
            return false;
        }
        String path = file.getPath();
        // Check for standard Maven/Gradle structure
        return (path.contains("/src/main/java/") || path.contains("\\src\\main\\java\\")) ||
               (path.contains("/src/test/java/") || path.contains("\\src\\test\\java\\"));
    }
}
