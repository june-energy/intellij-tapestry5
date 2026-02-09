/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TapestryLibraryMappingResolver}.
 *
 * @author June Energy
 * @since 1.0.0
 */
@DisplayName("TapestryLibraryMappingResolver")
class TapestryLibraryMappingResolverTest {

    @Nested
    @DisplayName("hasPrefixSeparator")
    class HasPrefixSeparatorTests {

        @Test
        @DisplayName("should return true for name with slash")
        void shouldReturnTrueForNameWithSlash() {
            assertTrue(TapestryLibraryMappingResolver.hasPrefixSeparator("ui/DatePicker"));
        }

        @Test
        @DisplayName("should return true for name with multiple slashes")
        void shouldReturnTrueForNameWithMultipleSlashes() {
            assertTrue(TapestryLibraryMappingResolver.hasPrefixSeparator("ui/sub/DatePicker"));
        }

        @Test
        @DisplayName("should return false for simple name")
        void shouldReturnFalseForSimpleName() {
            assertFalse(TapestryLibraryMappingResolver.hasPrefixSeparator("DatePicker"));
        }

        @Test
        @DisplayName("should return false for dot-separated name")
        void shouldReturnFalseForDotSeparatedName() {
            assertFalse(TapestryLibraryMappingResolver.hasPrefixSeparator("ui.DatePicker"));
        }

        @Test
        @DisplayName("should return false for empty string")
        void shouldReturnFalseForEmptyString() {
            assertFalse(TapestryLibraryMappingResolver.hasPrefixSeparator(""));
        }
    }

    @Nested
    @DisplayName("extractPrefix")
    class ExtractPrefixTests {

        @Test
        @DisplayName("should extract prefix from simple prefixed name")
        void shouldExtractPrefixFromSimplePrefixedName() {
            assertEquals("ui", TapestryLibraryMappingResolver.extractPrefix("ui/DatePicker"));
        }

        @Test
        @DisplayName("should extract prefix from multi-level path")
        void shouldExtractPrefixFromMultiLevelPath() {
            assertEquals("ui", TapestryLibraryMappingResolver.extractPrefix("ui/sub/DatePicker"));
        }

        @Test
        @DisplayName("should return null for name without prefix")
        void shouldReturnNullForNameWithoutPrefix() {
            assertNull(TapestryLibraryMappingResolver.extractPrefix("DatePicker"));
        }

        @Test
        @DisplayName("should return null for dot-separated name")
        void shouldReturnNullForDotSeparatedName() {
            assertNull(TapestryLibraryMappingResolver.extractPrefix("ui.DatePicker"));
        }

        @Test
        @DisplayName("should return null for empty string")
        void shouldReturnNullForEmptyString() {
            assertNull(TapestryLibraryMappingResolver.extractPrefix(""));
        }

        @Test
        @DisplayName("should return null for name starting with slash")
        void shouldReturnNullForNameStartingWithSlash() {
            assertNull(TapestryLibraryMappingResolver.extractPrefix("/DatePicker"));
        }
    }

    @Nested
    @DisplayName("Mixin resolution patterns")
    class MixinResolutionPatterns {

        @Test
        @DisplayName("should recognize ui/DatePicker as library prefixed")
        void shouldRecognizeLibraryPrefixedMixin() {
            String mixinName = "ui/DatePicker";
            assertTrue(TapestryLibraryMappingResolver.hasPrefixSeparator(mixinName));
            assertEquals("ui", TapestryLibraryMappingResolver.extractPrefix(mixinName));
        }

        @Test
        @DisplayName("should recognize core/autocomplete as library prefixed")
        void shouldRecognizeCoreLibraryPrefixedMixin() {
            String mixinName = "core/autocomplete";
            assertTrue(TapestryLibraryMappingResolver.hasPrefixSeparator(mixinName));
            assertEquals("core", TapestryLibraryMappingResolver.extractPrefix(mixinName));
        }

        @Test
        @DisplayName("should handle nested paths like ui/forms/DatePicker")
        void shouldHandleNestedPaths() {
            String mixinName = "ui/forms/DatePicker";
            assertTrue(TapestryLibraryMappingResolver.hasPrefixSeparator(mixinName));
            assertEquals("ui", TapestryLibraryMappingResolver.extractPrefix(mixinName));
        }
    }
}
