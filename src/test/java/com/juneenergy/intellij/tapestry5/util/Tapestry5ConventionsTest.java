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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Tapestry5Conventions}.
 *
 * <p>Tests the convention detection logic for Tapestry 5.x pages, components, and mixins.
 *
 * @author June Energy
 * @since 1.0.0
 */
@DisplayName("Tapestry5Conventions")
class Tapestry5ConventionsTest {

    @Nested
    @DisplayName("isInPagesPackage")
    class IsInPagesPackage {

        @ParameterizedTest
        @ValueSource(strings = {
                "com.example.pages.Index",
                "com.example.pages.admin.Dashboard",
                "myapp.pages.HomePage",
                "pages.SimplePage"
        })
        @DisplayName("should return true for classes in pages package")
        void shouldReturnTrueForPagesPackage(String qualifiedName) {
            assertThat(Tapestry5Conventions.isInPagesPackage(qualifiedName)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "com.example.components.MyComponent",
                "com.example.services.UserService",
                "com.example.Page",
                "com.example.pagesadmin.Dashboard"  // No dot before 'pages'
        })
        @DisplayName("should return false for classes not in pages package")
        void shouldReturnFalseForNonPagesPackage(String qualifiedName) {
            assertThat(Tapestry5Conventions.isInPagesPackage(qualifiedName)).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should return false for null or empty input")
        void shouldReturnFalseForNullOrEmpty(String qualifiedName) {
            assertThat(Tapestry5Conventions.isInPagesPackage(qualifiedName)).isFalse();
        }
    }

    @Nested
    @DisplayName("isInComponentsPackage")
    class IsInComponentsPackage {

        @ParameterizedTest
        @ValueSource(strings = {
                "com.example.components.Layout",
                "com.example.components.form.TextField",
                "myapp.components.Button",
                "components.SimpleComponent"
        })
        @DisplayName("should return true for classes in components package")
        void shouldReturnTrueForComponentsPackage(String qualifiedName) {
            assertThat(Tapestry5Conventions.isInComponentsPackage(qualifiedName)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "com.example.pages.Index",
                "com.example.services.ComponentService",
                "com.example.Component"
        })
        @DisplayName("should return false for classes not in components package")
        void shouldReturnFalseForNonComponentsPackage(String qualifiedName) {
            assertThat(Tapestry5Conventions.isInComponentsPackage(qualifiedName)).isFalse();
        }
    }

    @Nested
    @DisplayName("isInMixinsPackage")
    class IsInMixinsPackage {

        @ParameterizedTest
        @ValueSource(strings = {
                "com.example.mixins.Clickable",
                "com.example.mixins.validation.Required",
                "myapp.mixins.AutoFocus",
                "mixins.SimpleMixin"
        })
        @DisplayName("should return true for classes in mixins package")
        void shouldReturnTrueForMixinsPackage(String qualifiedName) {
            assertThat(Tapestry5Conventions.isInMixinsPackage(qualifiedName)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "com.example.pages.Index",
                "com.example.components.MyComponent",
                "com.example.Mixin"
        })
        @DisplayName("should return false for classes not in mixins package")
        void shouldReturnFalseForNonMixinsPackage(String qualifiedName) {
            assertThat(Tapestry5Conventions.isInMixinsPackage(qualifiedName)).isFalse();
        }
    }

    @Nested
    @DisplayName("getPackageName")
    class GetPackageName {

        @ParameterizedTest
        @CsvSource({
                "com.example.pages.Index, com.example.pages",
                "com.example.MyClass, com.example",
                "MyClass, ''",
                "'', ''"
        })
        @DisplayName("should extract package name from qualified name")
        void shouldExtractPackageName(String qualifiedName, String expectedPackage) {
            assertThat(Tapestry5Conventions.getPackageName(qualifiedName)).isEqualTo(expectedPackage);
        }

        @Test
        @DisplayName("should return empty string for null input")
        void shouldReturnEmptyForNull() {
            assertThat(Tapestry5Conventions.getPackageName(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSimpleClassName")
    class GetSimpleClassName {

        @ParameterizedTest
        @CsvSource({
                "com.example.pages.Index, Index",
                "com.example.MyClass, MyClass",
                "MyClass, MyClass",
                "'', ''"
        })
        @DisplayName("should extract simple class name from qualified name")
        void shouldExtractSimpleClassName(String qualifiedName, String expectedName) {
            assertThat(Tapestry5Conventions.getSimpleClassName(qualifiedName)).isEqualTo(expectedName);
        }

        @Test
        @DisplayName("should return empty string for null input")
        void shouldReturnEmptyForNull() {
            assertThat(Tapestry5Conventions.getSimpleClassName(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("TapestryElementType")
    class TapestryElementTypeTest {

        @Test
        @DisplayName("should have correct enum values")
        void shouldHaveCorrectEnumValues() {
            Tapestry5Conventions.TapestryElementType[] values = Tapestry5Conventions.TapestryElementType.values();
            assertThat(values).containsExactly(
                    Tapestry5Conventions.TapestryElementType.PAGE,
                    Tapestry5Conventions.TapestryElementType.COMPONENT,
                    Tapestry5Conventions.TapestryElementType.MIXIN,
                    Tapestry5Conventions.TapestryElementType.NONE
            );
        }
    }

    @Nested
    @DisplayName("Annotation Constants")
    class AnnotationConstants {

        @Test
        @DisplayName("should have correct annotation FQNs")
        void shouldHaveCorrectAnnotationFqns() {
            assertThat(Tapestry5Conventions.PAGE_ANNOTATION)
                    .isEqualTo("org.apache.tapestry5.annotations.Page");
            assertThat(Tapestry5Conventions.COMPONENT_ANNOTATION)
                    .isEqualTo("org.apache.tapestry5.annotations.Component");
            assertThat(Tapestry5Conventions.PROPERTY_ANNOTATION)
                    .isEqualTo("org.apache.tapestry5.annotations.Property");
            assertThat(Tapestry5Conventions.PARAMETER_ANNOTATION)
                    .isEqualTo("org.apache.tapestry5.annotations.Parameter");
        }
    }
}
