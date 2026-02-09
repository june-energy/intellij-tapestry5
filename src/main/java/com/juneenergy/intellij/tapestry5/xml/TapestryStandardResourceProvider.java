/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.xml;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;

/**
 * Registers Tapestry namespace URIs as standard resources to prevent
 * "URI is not registered" warnings in TML files.
 *
 * <p>This provider registers the following Tapestry namespaces:
 * <ul>
 *     <li>{@code http://tapestry.apache.org/schema/tapestry_5_4.xsd} - Tapestry 5.4+ component namespace</li>
 *     <li>{@code http://tapestry.apache.org/schema/tapestry_5_3.xsd} - Tapestry 5.3 component namespace</li>
 *     <li>{@code http://tapestry.apache.org/schema/tapestry_5_1_0.xsd} - Tapestry 5.1 component namespace</li>
 *     <li>{@code http://tapestry.apache.org/schema/tapestry_5_0_0.xsd} - Tapestry 5.0 component namespace</li>
 *     <li>{@code tapestry:parameter} - Tapestry parameter namespace</li>
 * </ul>
 *
 * <p>By registering these URIs as "ignored" resources, IntelliJ will not show
 * "URI is not registered" warnings, while still allowing the
 * {@link TapestryImplicitNamespaceDescriptorProvider} to provide element/attribute completion.
 *
 * @author June Energy
 * @since 1.0.0
 */
public class TapestryStandardResourceProvider implements StandardResourceProvider {

    @Override
    public void registerResources(ResourceRegistrar registrar) {
        // Register Tapestry schema URIs as ignored resources
        // This tells IntelliJ not to warn about these URIs being unregistered
        
        // Tapestry 5.4+ (current)
        registrar.addIgnoredResource(TapestryImplicitNamespaceDescriptorProvider.TAPESTRY_5_4_NAMESPACE);
        
        // Tapestry 5.3
        registrar.addIgnoredResource(TapestryImplicitNamespaceDescriptorProvider.TAPESTRY_5_3_NAMESPACE);
        
        // Tapestry 5.1
        registrar.addIgnoredResource(TapestryImplicitNamespaceDescriptorProvider.TAPESTRY_5_1_NAMESPACE);
        
        // Tapestry 5.0
        registrar.addIgnoredResource(TapestryImplicitNamespaceDescriptorProvider.TAPESTRY_5_0_NAMESPACE);
        
        // Tapestry parameter namespace
        registrar.addIgnoredResource(TapestryImplicitNamespaceDescriptorProvider.TAPESTRY_PARAMETER_NAMESPACE);
        
        // Common library namespace patterns
        // Note: StandardResourceProvider doesn't support wildcards, so we register common ones
        registrar.addIgnoredResource("tapestry-library:core");
        registrar.addIgnoredResource("tapestry-library:common");
    }
}
