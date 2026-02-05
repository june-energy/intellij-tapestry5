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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Tapestry5FileUtils}.
 *
 * <p>Tests file type detection and utility methods.
 * Note: Tests involving VirtualFile and PsiFile require IntelliJ Platform test fixtures.
 *
 * @author June Energy
 * @since 1.0.0
 */
@DisplayName("Tapestry5FileUtils")
class Tapestry5FileUtilsTest {

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("TML_EXTENSION should be 'tml'")
        void tmlExtensionShouldBeTml() {
            assertThat(Tapestry5FileUtils.TML_EXTENSION).isEqualTo("tml");
        }

        @Test
        @DisplayName("JAVA_EXTENSION should be 'java'")
        void javaExtensionShouldBeJava() {
            assertThat(Tapestry5FileUtils.JAVA_EXTENSION).isEqualTo("java");
        }
    }

    @Nested
    @DisplayName("isTmlFile with VirtualFile")
    class IsTmlFileVirtual {

        @Test
        @DisplayName("should return false for null VirtualFile")
        void shouldReturnFalseForNullVirtualFile() {
            assertThat(Tapestry5FileUtils.isTmlFile((com.intellij.openapi.vfs.VirtualFile) null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isTmlFile with PsiFile")
    class IsTmlFilePsi {

        @Test
        @DisplayName("should return false for null PsiFile")
        void shouldReturnFalseForNullPsiFile() {
            assertThat(Tapestry5FileUtils.isTmlFile((com.intellij.psi.PsiFile) null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isJavaFile with VirtualFile")
    class IsJavaFileVirtual {

        @Test
        @DisplayName("should return false for null VirtualFile")
        void shouldReturnFalseForNullVirtualFile() {
            assertThat(Tapestry5FileUtils.isJavaFile((com.intellij.openapi.vfs.VirtualFile) null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isJavaFile with PsiFile")
    class IsJavaFilePsi {

        @Test
        @DisplayName("should return false for null PsiFile")
        void shouldReturnFalseForNullPsiFile() {
            assertThat(Tapestry5FileUtils.isJavaFile((com.intellij.psi.PsiFile) null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getFileNameWithoutExtension")
    class GetFileNameWithoutExtension {

        @Test
        @DisplayName("should return null for null VirtualFile")
        void shouldReturnNullForNullVirtualFile() {
            assertThat(Tapestry5FileUtils.getFileNameWithoutExtension((com.intellij.openapi.vfs.VirtualFile) null)).isNull();
        }

        @Test
        @DisplayName("should return null for null PsiFile")
        void shouldReturnNullForNullPsiFile() {
            assertThat(Tapestry5FileUtils.getFileNameWithoutExtension((com.intellij.psi.PsiFile) null)).isNull();
        }
    }

    @Nested
    @DisplayName("isInResourcesDirectory")
    class IsInResourcesDirectory {

        @Test
        @DisplayName("should return false for null file")
        void shouldReturnFalseForNull() {
            assertThat(Tapestry5FileUtils.isInResourcesDirectory(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isInSourceDirectory")
    class IsInSourceDirectory {

        @Test
        @DisplayName("should return false for null file")
        void shouldReturnFalseForNull() {
            assertThat(Tapestry5FileUtils.isInSourceDirectory(null)).isFalse();
        }
    }
}
