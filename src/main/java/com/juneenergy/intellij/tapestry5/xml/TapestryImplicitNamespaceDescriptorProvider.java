/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.xml;

import com.intellij.javaee.ImplicitNamespaceDescriptorProvider;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlNSDescriptor;
import com.juneenergy.intellij.tapestry5.util.Tapestry5FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides implicit namespace descriptors for Tapestry namespaces in TML files.
 *
 * <p>This provider recognizes the following Tapestry namespaces:
 * <ul>
 *     <li>{@code http://tapestry.apache.org/schema/tapestry_5_4.xsd} - Tapestry component namespace</li>
 *     <li>{@code tapestry:parameter} - Tapestry parameter namespace</li>
 * </ul>
 *
 * <p>By providing implicit descriptors for these namespaces, IntelliJ will no longer show
 * "URI is not registered" warnings for Tapestry TML files.
 *
 * @author June Energy
 * @since 1.0.0
 */
public class TapestryImplicitNamespaceDescriptorProvider implements ImplicitNamespaceDescriptorProvider {

    /** The Tapestry 5.4 namespace URI. */
    public static final String TAPESTRY_5_4_NAMESPACE = "http://tapestry.apache.org/schema/tapestry_5_4.xsd";

    /** The Tapestry 5.3 namespace URI (for backward compatibility). */
    public static final String TAPESTRY_5_3_NAMESPACE = "http://tapestry.apache.org/schema/tapestry_5_3.xsd";

    /** The Tapestry 5.1 namespace URI (for backward compatibility). */
    public static final String TAPESTRY_5_1_NAMESPACE = "http://tapestry.apache.org/schema/tapestry_5_1_0.xsd";

    /** The Tapestry 5.0 namespace URI (for backward compatibility). */
    public static final String TAPESTRY_5_0_NAMESPACE = "http://tapestry.apache.org/schema/tapestry_5_0_0.xsd";

    /** The Tapestry parameter namespace URI. */
    public static final String TAPESTRY_PARAMETER_NAMESPACE = "tapestry:parameter";

    /** The Tapestry library namespace URI. */
    public static final String TAPESTRY_LIBRARY_NAMESPACE = "tapestry-library:";

    /**
     * Returns a namespace descriptor for the given namespace in the specified file.
     *
     * <p>This method returns a permissive descriptor for recognized Tapestry namespaces,
     * which allows any tag and attribute under those namespaces.
     *
     * @param module the module context
     * @param namespace the namespace URI to check
     * @param file the PSI file being processed
     * @return a namespace descriptor if this is a Tapestry namespace in a TML file, otherwise null
     */
    @Override
    @Nullable
    public XmlNSDescriptor getNamespaceDescriptor(@Nullable Module module,
                                                   @NotNull String namespace,
                                                   @Nullable PsiFile file) {
        // Only provide descriptors for TML files
        if (file == null || !Tapestry5FileUtils.isTmlFile(file)) {
            return null;
        }

        // Check if this is a Tapestry namespace
        if (isTapestryNamespace(namespace)) {
            return new TapestryPermissiveNSDescriptor(namespace);
        }

        return null;
    }

    /**
     * Checks if the given namespace URI is a Tapestry namespace.
     *
     * @param namespace the namespace URI to check
     * @return true if this is a recognized Tapestry namespace
     */
    private boolean isTapestryNamespace(@NotNull String namespace) {
        return TAPESTRY_PARAMETER_NAMESPACE.equals(namespace) ||
               TAPESTRY_5_4_NAMESPACE.equals(namespace) ||
               TAPESTRY_5_3_NAMESPACE.equals(namespace) ||
               TAPESTRY_5_1_NAMESPACE.equals(namespace) ||
               TAPESTRY_5_0_NAMESPACE.equals(namespace) ||
               namespace.startsWith(TAPESTRY_LIBRARY_NAMESPACE) ||
               namespace.startsWith("http://tapestry.apache.org/");
    }
}
