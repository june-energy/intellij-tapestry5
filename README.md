# Tapestry 5.9 IntelliJ Plugin

[![Build Status](https://github.com/june-energy/intellij-tapestry5/workflows/Build/badge.svg)](https://github.com/june-energy/intellij-tapestry5/actions)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.juneenergy.intellij.tapestry5.svg)](https://plugins.jetbrains.com/plugin/com.juneenergy.intellij.tapestry5)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

IntelliJ IDEA plugin providing comprehensive support for [Apache Tapestry 5.9](https://tapestry.apache.org/) web framework.

![Apache Tapestry Logo](src/main/resources/icons/apache_tapestry_full.svg)

## Features

### Navigation
- **File Switcher**: Quick navigation between Java classes and TML templates (`Ctrl+Alt+Shift+T` / `Cmd+Alt+Shift+T`)
- **Go to Declaration**: Navigate from component references in TML to their Java classes
- **Line Markers**: Gutter icons linking Java classes ↔ TML templates and component references

### Code Completion
- **Component Completion**: Auto-complete component names in `t:type` attributes and `t:` prefixed tags
- **Mixin Completion**: Auto-complete mixin names in `t:mixins` attributes
- **Built-in Components**: Includes all Tapestry core components (ActionLink, Loop, If, etc.)
- **Project Components**: Discovers and suggests components/mixins from your project

### Inspections & Quick Fixes
- **Missing Template Inspection**: Warns when a Java page/component class is missing its TML template
- **Create Template Quick Fix**: Automatically generates the missing TML template file
- **Implicit Usage Provider**: Prevents false "unused" warnings on Tapestry classes with templates

### XML Namespace Support
- **Automatic Namespace Registration**: No more "URI is not registered" warnings for Tapestry namespaces
- **Permissive Validation**: TML files with Tapestry-specific attributes validate without errors

Forked from [Tapestry 4 IntelliJ Plugin](https://github.com/linuxswords/intellij-tapestry4)

## Supported Tapestry 5.x Features

| Feature | Support |
|---------|---------|
| TML templates (`.tml` files) | ✅ |
| Convention-based pages (`pages` package) | ✅ |
| Convention-based components (`components` package) | ✅ |
| Convention-based mixins (`mixins` package) | ✅ |
| Component references (`t:type="..."`) | ✅ |
| Component tags (`<t:component>`) | ✅ |
| Mixin references (`t:mixins="..."`) | ✅ |
| Library prefix components (`core/ActionLink`) | ✅ |
| Property expressions (`${...}`) | 🚧 Planned |
| Event handlers (`onEvent...`) | 🚧 Planned |

## Installation

### From JetBrains Marketplace

1. Open IntelliJ IDEA
2. Go to **Settings/Preferences** → **Plugins** → **Marketplace**
3. Search for "Tapestry 5.9 Support"
4. Click **Install**

### Manual Installation

1. Download the latest release from [GitHub Releases](https://github.com/june-energy/intellij-tapestry5/releases)
2. Go to **Settings/Preferences** → **Plugins** → **⚙️** → **Install Plugin from Disk...**
3. Select the downloaded `.zip` file

## Usage

### File Switcher

Use the keyboard shortcut `Ctrl+Alt+Shift+T` (or `Cmd+Alt+Shift+T` on macOS) to switch between:
- Java page/component class ↔ TML template

You can also access this via **Navigate** → **Switch Tapestry TML/Java** or **Tools** menu.

### Code Completion

In TML files, the plugin provides completion for:

```xml
<!-- Component type completion -->
<div t:type="Act[cursor]">  <!-- Suggests ActionLink, ActionForm, etc. -->

<!-- Tapestry tag completion -->
<t:act[cursor]>  <!-- Suggests t:actionlink, t:alerts, etc. -->

<!-- Mixin completion -->
<input t:type="TextField" t:mixins="auto[cursor]"/>  <!-- Suggests Autocomplete -->
```

### Navigation

- **Ctrl+Click** (or **Cmd+Click**) on a component name to navigate to its Java class
- Click the **gutter icon** next to a Java class to open its TML template
- Click the **gutter icon** in a TML file to open the corresponding Java class

### Tapestry Conventions

The plugin follows Tapestry 5.x conventions:

| Element | Location | Template |
|---------|----------|----------|
| Pages | `*.pages` package | Same package, `.tml` extension |
| Components | `*.components` package | Same package, `.tml` extension |
| Mixins | `*.mixins` package | N/A (no templates) |

Example:
- `com.example.pages.Index` → `com/example/pages/Index.tml`
- `com.example.components.MyComponent` → `com/example/components/MyComponent.tml`

## Requirements

- IntelliJ IDEA 2024.1 or later (Community or Ultimate)
- Java 17+ for plugin development

## Building from Source

```bash
# Clone the repository
git clone https://github.com/june-energy/intellij-tapestry5.git
cd intellij-tapestry5

# Build the plugin
./gradlew build

# Run IntelliJ IDEA with the plugin
./gradlew runIde

# Run tests
./gradlew test

# Build distribution zip
./gradlew buildPlugin
```

## Project Structure

```
src/main/java/com/juneenergy/intellij/tapestry5/
├── Tapestry5Switcher.java          # File switching action
├── completion/                      # Code completion
│   └── Tapestry5CompletionContributor.java
├── inspections/                     # Code inspections
│   ├── MissingTemplateInspection.java
│   └── TapestryImplicitUsageProvider.java
├── markers/                         # Gutter line markers
│   ├── Tapestry5LineMarkerProvider.java
│   ├── TmlLineMarkerProvider.java
│   └── TmlComponentLineMarkerProvider.java
├── references/                      # PSI references
│   ├── TapestryComponentReference.java
│   ├── TapestryMixinReference.java
│   └── TmlReferenceContributor.java
├── util/                            # Utility classes
│   ├── Tapestry5Conventions.java
│   ├── Tapestry5FileUtils.java
│   ├── Tapestry5PluginHelper.java
│   ├── TapestryComponentResolver.java
│   └── TapestryLibraryMappingResolver.java
└── xml/                             # XML namespace support
    ├── TapestryImplicitNamespaceDescriptorProvider.java
    ├── TapestryPermissiveNSDescriptor.java
    └── TapestryStandardResourceProvider.java
```

## Contributing

Contributions are welcome! Please read our contributing guidelines first.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Based on the legacy [Tapestry 4.1 IntelliJ Plugin](https://github.com/linuxswords/intellij-tapestry4)
- Inspired by the Apache Tapestry community

## Links

- [Apache Tapestry Documentation](https://tapestry.apache.org/documentation.html)
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Issue Tracker](https://github.com/june-energy/intellij-tapestry5/issues)
