/*
 * Tapestry 5.9 IntelliJ Plugin - Gradle Settings
 *
 * This file defines the project name and configures the Gradle plugin repositories.
 */

rootProject.name = "intellij-tapestry5"

pluginManagement {
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
