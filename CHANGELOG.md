# Changelog

All notable changes to the Tapestry 5.9 IntelliJ Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

#### Navigation
- **File Switcher Action** (`Ctrl+Alt+Shift+T`) for quick navigation between Java ↔ TML files
- **Go to Declaration** from component references (`t:type` attributes) to Java classes
- **Go to Declaration** from mixin references (`t:mixins` attributes) to Java classes
- **Go to Declaration** from Tapestry tags (`<t:component>`) to Java classes

#### Code Completion
- Component name completion in `t:type` attributes
- Component tag completion for `<t:tagname>` elements
- Mixin name completion in `t:mixins` attributes
- Built-in Tapestry core components (ActionLink, Loop, If, Zone, etc.)
- Project component and mixin discovery

#### Line Markers (Gutter Icons)
- Java class → TML template navigation marker
- TML template → Java class navigation marker
- Component reference markers in TML files (for `t:type` and `<t:component>` tags)
- Mixin reference markers in TML files

#### Inspections & Quick Fixes
- **Missing Template Inspection**: Warns when a page/component class is missing its TML file
- **Create Template Quick Fix**: Generates the missing TML template with proper structure
- **Implicit Usage Provider**: Marks Tapestry classes with linked templates as "used"

#### XML Namespace Support
- Automatic registration of Tapestry schema URIs
- No more "URI is not registered" warnings for:
  - `http://tapestry.apache.org/schema/tapestry_5_4.xsd`
  - `tapestry:parameter`
  - `tapestry-library:*` prefixes
- Permissive namespace descriptor for TML validation

#### Utilities
- `Tapestry5Conventions` - Detection of pages, components, mixins by package/annotation
- `Tapestry5FileUtils` - TML/Java file detection and naming utilities
- `Tapestry5PluginHelper` - File navigation and notification helpers
- `TapestryComponentResolver` - Component/mixin class resolution
- `TapestryLibraryMappingResolver` - Library prefix mapping resolution

### Technical
- Target IntelliJ IDEA 2024.1+
- Requires Java 17+
- Uses Gradle 8.13+ with Kotlin DSL
- IntelliJ Platform Gradle Plugin 2.x

## [1.0.0] - TBD

### Planned Features
- Property expression completion (`${...}`, `prop:`)
- Property expression navigation to Java getters/fields
- Event handler method completion (`onEventFromComponent`)
- Event handler navigation markers
- Missing Java class inspection for TML files
- Invalid property expression inspection

---

## Migration from Tapestry 4.1 Plugin

This plugin is a complete rewrite of the legacy Tapestry 4.1 IntelliJ plugin, updated for:

### Tapestry 5.x Architecture
| Tapestry 4.1 | Tapestry 5.x |
|--------------|--------------|
| HTML with `jwcid` | TML templates |
| JWC component definitions | Convention-based detection |
| OGNL expressions | Property expressions (`${...}`) |
| `listener:` bindings | `onEvent...` methods |

### Modern IntelliJ Platform
- IntelliJ Platform Gradle Plugin 2.x (was legacy devkit)
- Java 17+ requirement (was Java 8)
- Modern API usage (no deprecated APIs)
- Proper threading with read/write actions

### Improved Codebase
- Comprehensive documentation
- Unit tests for utility classes
- Clean separation of concerns
- No external dependencies (removed Guava, OGNL)

[Unreleased]: https://github.com/june-energy/intellij-tapestry5/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/june-energy/intellij-tapestry5/releases/tag/v1.0.0
