# Changelog

All notable changes to the Tapestry 5.9 IntelliJ Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial plugin structure with IntelliJ Platform Gradle Plugin 2.x
- Tapestry 5.x convention detection (pages, components, mixins packages)
- File switcher action (Ctrl+Alt+Shift+T) for Java ↔ TML navigation
- Gutter icons linking Java classes to TML templates
- Gutter icons linking TML templates to Java classes
- Utility classes for file type detection and Tapestry conventions
- Message bundle for localization support
- Plugin icon (SVG format)

### Technical
- Target IntelliJ IDEA 2024.1+
- Requires Java 17+
- Uses Gradle 8.13+ with Kotlin DSL

## [1.0.0] - TBD

### Added
- **File Switcher**: Quick navigation between Java and TML files
  - Keyboard shortcut: `Ctrl+Alt+Shift+T`
  - Available in Navigate menu and Tools menu
  - Automatically finds matching files based on naming conventions

- **Line Markers**: Visual navigation cues in the gutter
  - HTML icon on Java classes with matching TML templates
  - Java icon on TML templates with matching Java classes
  - Click to navigate to partner file

- **Convention Support**: Detects Tapestry elements by package location
  - `pages` package → Page detection
  - `components` package → Component detection
  - `mixins` package → Mixin detection

### Supported Tapestry Features
- Property expressions: `${property}`, `prop:expression`
- Page/component/mixin detection via package conventions
- Template file association: `ClassName.java` ↔ `ClassName.tml`

### Not Yet Implemented
- Code completion in TML property expressions
- Go to Declaration from TML to Java
- Reference resolution and rename refactoring
- Inspections for missing templates/classes
- Event handler navigation

## Migration from Tapestry 4.1 Plugin

This plugin is a complete rewrite of the legacy Tapestry 4.1 IntelliJ plugin,
updated for:

1. **Tapestry 5.x architecture**
   - TML templates instead of HTML with jwcid
   - Convention-based detection instead of JWC files
   - Property expressions instead of OGNL

2. **Modern IntelliJ Platform**
   - IntelliJ Platform Gradle Plugin 2.x
   - Java 17+ requirement
   - Modern API usage (no deprecated APIs)

3. **Improved codebase**
   - Comprehensive documentation
   - Unit tests for utility classes
   - Clean separation of concerns

[Unreleased]: https://github.com/june-energy/intellij-tapestry5/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/june-energy/intellij-tapestry5/releases/tag/v1.0.0
