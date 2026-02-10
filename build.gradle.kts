/*
 * Tapestry 5.9 IntelliJ Plugin - Build Configuration
 *
 * This file configures the Gradle build for the IntelliJ Platform Plugin.
 * Uses the IntelliJ Platform Gradle Plugin 2.x for modern plugin development.
 *
 * @see https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
 */

plugins {
    id("java")
    alias(libs.plugins.intellij.platform)
}

// Read properties from gradle.properties
val pluginVersion: String by project
val pluginGroup: String by project
val pluginId: String by project
val platformVersion: String by project
val javaVersion: String by project

group = pluginGroup
version = pluginVersion

repositories {
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension
    // Provides access to IntelliJ Platform releases, snapshots, and plugin dependencies
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // IntelliJ Platform dependency
    intellijPlatform {
        // Target IntelliJ IDEA Community Edition
        intellijIdeaCommunity(platformVersion)

        // Plugin dependencies - bundled plugins required for our plugin
        bundledPlugin("com.intellij.java")

        // Plugin Verifier for compatibility checks
        pluginVerifier()
    }

    // Testing dependencies - JUnit 5
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj.core)
}

// Java compilation configuration
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

// IntelliJ Platform configuration
intellijPlatform {
    // Plugin configuration from plugin.xml
    pluginConfiguration {
        id = pluginId
        name = "Tapestry 5.9 Support"
        version = pluginVersion
        description = """
            Support for Apache Tapestry 5.9 web framework.
            
            Features:
            - Switch between Java and TML files (Ctrl+Alt+Shift+T)
            - Code completion in TML templates
            - Navigation to Java from property expressions
            - Component reference resolution
            - Line markers for related files
            
            Tapestry 5.x Support:
            - Property expressions: ${'$'}{property}, prop:
            - Convention-based page/component detection
            - Annotation support: @Page, @Component, @Property, @Parameter
        """

        changeNotes = """
            1.0.0:
            - Initial release
            - File switcher between Java and TML files
            - Basic code completion for property expressions
            - Navigation from TML to Java classes
        """

        ideaVersion {
            // Compatibility range
            sinceBuild = "241"      // 2024.1
            untilBuild = provider { null }  // No upper limit (compatible with all future versions)
        }

        vendor {
            name = "June Energy"
            email = "dev@june.energy"
            url = "https://github.com/june-energy"
        }
    }

    // Plugin verification configuration
    pluginVerification {
        ides {
            // Use specific IDE versions that exist, based on platformVersion (2024.1)
            // Format: "IC-2024.1" for IntelliJ IDEA Community
            ide("IC", platformVersion)
        }
    }

    // Publishing configuration (for JetBrains Marketplace)
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

// Test configuration
tasks {
    test {
        useJUnitPlatform()
    }

    // Ensure we build with the correct Java version
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-serial"))
    }
}
