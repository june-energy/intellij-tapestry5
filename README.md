# Tapestry 5.9 IntelliJ Plugin

[![Build Status](https://github.com/june-energy/intellij-tapestry5/workflows/Build/badge.svg)](https://github.com/june-energy/intellij-tapestry5/actions)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.juneenergy.intellij.tapestry5.svg)](https://plugins.jetbrains.com/plugin/com.juneenergy.intellij.tapestry5)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

IntelliJ IDEA plugin providing support for [Apache Tapestry 5.9](https://tapestry.apache.org/) web framework.

![Apache TapestryLogo](src/main/resources/icons/apache_tapestry_full.svg)



## Features

- **File Switcher**: Quick navigation between Java classes and TML templates (`Ctrl+Alt+Shift+T`)
- **Code Completion**: Property expression completion in TML files
- **Navigation**: Go to Declaration from property expressions to Java code
- **Line Markers**: Gutter icons linking Java classes to their TML templates
- **Inspections**: Detect missing templates, invalid property expressions

Forked from [Tapestry 4 IntelliJ Plugin](https://github.com/linuxswords/intellij-tapestry4)

## Supported Tapestry 5.x Features

| Feature | Support |
|---------|---------|
| Property expressions (`${...}`) | ✅ |
| Prefix bindings (`prop:`, `literal:`, `message:`) | ✅ |
| Convention-based pages/components | ✅ |
| `@Property` annotation | ✅ |
| `@Parameter` annotation | ✅ |
| `@Component` annotation | ✅ |
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

### Tapestry Conventions

The plugin uses Tapestry 5.x conventions to find related files:

- Templates are located in the same package as their Java class
- Template files have the same name as the Java class with `.tml` extension
- Pages are in `pages` package, components in `components` package

## Requirements

- IntelliJ IDEA 2024.1 or later
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
```

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) first.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Based on the legacy [Tapestry 4.1 IntelliJ Plugin](legacy/)
- Inspired by the Apache Tapestry community

## Links

- [Apache Tapestry Documentation](https://tapestry.apache.org/documentation.html)
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Issue Tracker](https://github.com/june-energy/intellij-tapestry5/issues)
