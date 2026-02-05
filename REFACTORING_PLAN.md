# Tapestry 5.9 IntelliJ Plugin - Refactoring Plan

## Executive Summary

This document outlines the plan to refactor the legacy Tapestry 4.1 IntelliJ plugin into a modern IntelliJ plugin supporting **Apache Tapestry 5.9**. The refactoring involves both modernizing the plugin infrastructure (moving from legacy IntelliJ plugin format to Gradle-based IntelliJ Platform Plugin) and adapting the functionality for Tapestry 5.x architecture.

---

## Part 1: Analysis of Legacy Plugin

### Current Features (Tapestry 4.1)

| Feature | Implementation | Description |
|---------|---------------|-------------|
| **File Switcher** | `TapestrySwitcher.java` | Switches between HTML ↔ Java ↔ JWC files (Ctrl+Alt+Shift+L) |
| **Code Completion** | `PropertyCompletionContributor.java` | Completes OGNL expressions in HTML files |
| **Go to Declaration** | `OgnlGoToDeclarationHandler.java` | Navigate from OGNL expressions to Java methods |
| **JWC Navigation** | `JwcidGoToComponentDeclarationHandler.java` | Navigate from jwcid attributes to component classes |
| **Partner File Finding** | `PartnerElementFinder` hierarchy | Finds related HTML/Java/JWC files |

### Legacy Dependencies
- Google Guava 17.0
- Mockito (for tests)
- OGNL library (for parsing Tapestry 4.x expressions)

### Tapestry 4.1 vs 5.x Key Differences

| Aspect | Tapestry 4.1 | Tapestry 5.9 |
|--------|-------------|--------------|
| **Expression Language** | OGNL (`ognl:`, `listener:`, `action:`) | Property Expressions (`${...}`, `prop:`, `literal:`) |
| **Template Files** | `.html` with `jwcid` attributes | `.tml` (Tapestry Markup Language) |
| **Component Definition** | `.jwc` XML files | Java annotations (`@Component`) |
| **Base Classes** | `org.apache.tapestry.IPage`, `org.apache.tapestry.internal.Component` | Convention-based (no required base class) |
| **Page Detection** | Extends `IPage` | Classes in `pages` package or `@Page` annotation |
| **Component Detection** | Extends `Component` + JWC | Classes in `components` package or `@Component` annotation |
| **Property Binding** | OGNL expressions | `@Property`, `@Parameter`, getter/setter conventions |
| **Event Handlers** | `listener:methodName` | `onEventFromComponent()` naming convention |

---

## Part 2: Modern IntelliJ Plugin Structure

### Requirements (IntelliJ Platform Gradle Plugin 2.x)

- **IntelliJ Platform**: 2022.3+ (recommend targeting 2024.1+)
- **Gradle**: 8.13+
- **Java Runtime**: 17+
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)

### Target Project Structure

```
intellij-tapestry5/
├── .github/                          # GitHub Actions workflows
│   └── workflows/
│       └── build.yml
├── .run/                             # Run/Debug configurations
├── gradle/
│   ├── wrapper/
│   └── libs.versions.toml            # Gradle version catalog
├── src/
│   ├── main/
│   │   ├── java/                     # Java sources
│   │   │   └── com/
│   │   │       └── juneenergy/
│   │   │           └── intellij/
│   │   │               └── tapestry5/
│   │   │                   ├── Tapestry5Switcher.java
│   │   │                   ├── completion/
│   │   │                   ├── navigation/
│   │   │                   ├── references/
│   │   │                   ├── intentions/
│   │   │                   └── util/
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml
│   │       └── icons/
│   │           └── tapestry5.svg     # Modern SVG icon
│   └── test/
│       ├── java/                     # Test sources
│       └── testData/                 # Test fixtures
├── legacy/                           # Keep for reference
├── build.gradle.kts                  # Gradle build configuration
├── settings.gradle.kts               # Gradle settings
├── gradle.properties                 # Gradle properties
├── CHANGELOG.md
├── LICENSE
└── README.md
```

---

## Part 3: Step-by-Step Refactoring Plan

### Phase 1: Project Setup (Infrastructure)

#### Step 1.1: Initialize Gradle Project
- [ ] Create `settings.gradle.kts` with project name
- [ ] Create `gradle.properties` with version info
- [ ] Create `gradle/libs.versions.toml` for dependency management
- [ ] Add Gradle wrapper (8.13+)

#### Step 1.2: Configure build.gradle.kts
- [ ] Apply `org.jetbrains.intellij.platform` plugin
- [ ] Configure target IntelliJ version (2024.1+)
- [ ] Set up Java 17 compilation
- [ ] Configure plugin metadata (id, name, version, vendor)
- [ ] Add dependencies for Java and HTML language support

#### Step 1.3: Create plugin.xml
- [ ] Define plugin ID: `com.juneenergy.intellij.tapestry5`
- [ ] Set compatibility range (since-build, until-build)
- [ ] Declare dependencies on `com.intellij.modules.java` and `com.intellij.modules.xml`
- [ ] Register all extension points

---

### Phase 2: Core Infrastructure (Utility Classes)

#### Step 2.1: File Type Utilities
- [ ] Create `Tapestry5FileUtils.java`
  - Detect `.tml` template files
  - Detect Java page/component files
  - Check file location conventions (pages/, components/, mixins/)

#### Step 2.2: Tapestry Convention Utilities  
- [ ] Create `Tapestry5Conventions.java`
  - Page detection: class in `pages` package or `@Page` annotation
  - Component detection: class in `components` package
  - Mixin detection: class in `mixins` package
  - Template location resolution (same package, same name with .tml)

#### Step 2.3: Plugin Helper Utilities
- [ ] Create `Tapestry5PluginHelper.java`
  - File search utilities
  - Notification utilities (use modern NotificationGroup API)
  - PSI utilities for Java introspection

---

### Phase 3: Partner File Navigation (File Switcher)

#### Step 3.1: Refactor Partner Element Finders
- [ ] Create `Tapestry5PartnerElementFinder.java` (abstract base)
- [ ] Create `TmlPartnerElementFinder.java` (TML → Java)
- [ ] Create `JavaPartnerElementFinder.java` (Java → TML)
- [ ] Remove JWC-related finders (not used in Tapestry 5)

#### Step 3.2: Implement File Switcher Action
- [ ] Create `Tapestry5Switcher.java`
  - Switch between `.tml` ↔ `.java` files
  - Handle convention-based naming (MyPage.java ↔ MyPage.tml)
  - Support components and pages
- [ ] Register keyboard shortcut (Ctrl+Alt+Shift+T or similar)

---

### Phase 4: Code Completion

#### Step 4.1: Property Expression Completion
- [ ] Create `Tapestry5CompletionContributor.java`
  - Complete inside `${...}` expressions
  - Suggest properties from Java class (`@Property` fields, getters)
  - Suggest component parameters (`@Parameter` fields)

#### Step 4.2: Component ID Completion
- [ ] Complete `t:id` attribute values
- [ ] Suggest component types for `t:type` attributes

#### Step 4.3: Event Handler Completion
- [ ] Complete event handler method names (`onEventFromComponentId`)

---

### Phase 5: Navigation (Go to Declaration)

#### Step 5.1: Property Expression Navigation
- [ ] Create `PropertyExpressionGoToDeclarationHandler.java`
  - Navigate from `${propertyName}` to getter/field in Java
  - Navigate from `prop:propertyName` to Java member

#### Step 5.2: Component Reference Navigation
- [ ] Create `ComponentReferenceGoToDeclarationHandler.java`
  - Navigate from `t:id="componentId"` to `@Component` field
  - Navigate from `t:type="ComponentName"` to component class

#### Step 5.3: Event Handler Navigation
- [ ] Navigate from TML event references to Java handler methods

---

### Phase 6: Reference Resolution & Renaming

#### Step 6.1: Property References
- [ ] Create `Tapestry5PropertyReference.java`
  - Support Find Usages for properties used in TML
  - Support Rename refactoring

#### Step 6.2: Component ID References
- [ ] Create `ComponentIdReference.java`
  - Link `t:id` values to Java `@Component` fields
  - Support rename refactoring

---

### Phase 7: Inspections & Quick Fixes

#### Step 7.1: Missing Template Inspection
- [ ] Warn if Java page/component has no corresponding `.tml`
- [ ] Quick fix: Create template file

#### Step 7.2: Missing Java Class Inspection
- [ ] Warn if `.tml` template has no corresponding Java class
- [ ] Quick fix: Create Java class

#### Step 7.3: Invalid Property Expression
- [ ] Warn if `${property}` references non-existent member
- [ ] Quick fix: Create getter/property

---

### Phase 8: Line Markers & Gutter Icons

#### Step 8.1: Related File Line Markers
- [ ] Create `Tapestry5LineMarkerProvider.java`
  - Show gutter icon on Java class with link to TML
  - Show gutter icon on TML with link to Java class

#### Step 8.2: Event Handler Markers
- [ ] Mark event handler methods with icon
- [ ] Link to TML event sources

---

### Phase 9: Testing

#### Step 9.1: Unit Tests
- [ ] Test convention detection utilities
- [ ] Test partner file finding logic
- [ ] Test expression parsing

#### Step 9.2: Integration Tests
- [ ] Test completion in TML files
- [ ] Test navigation from TML to Java
- [ ] Test rename refactoring

#### Step 9.3: Test Fixtures
- [ ] Create sample Tapestry 5.9 project structure
- [ ] Include pages, components, mixins examples

---

### Phase 10: Documentation & Release

#### Step 10.1: Documentation
- [ ] Update README.md with features and installation
- [ ] Create CHANGELOG.md
- [ ] Add usage documentation

#### Step 10.2: Release Preparation
- [ ] Configure plugin signing
- [ ] Set up GitHub Actions for CI/CD
- [ ] Prepare for JetBrains Marketplace submission

---

## Part 4: Technical Implementation Details

### 4.1 Tapestry 5.x Expression Syntax

```
// Property binding
${message}              → getMessage() or @Property String message
${user.name}            → getUser().getName()

// Literal
literal:Hello World     → Literal string

// Property prefix (explicit)
prop:message            → Same as ${message}

// Asset binding
asset:images/logo.png   → Asset path

// Message binding  
message:greeting        → Message from .properties file

// Symbol binding
symbol:tapestry.version → Tapestry symbol value
```

### 4.2 Tapestry 5.x Annotations to Support

```java
@Page                   // Marks a page class
@Component              // Embeds a component
@Parameter              // Component parameter
@Property               // Auto-generate getter/setter
@Persist                // Persist across requests
@InjectComponent        // Inject embedded component
@InjectPage             // Inject another page
@OnEvent                // Event handler
@Cached                 // Cache method result
```

### 4.3 Modern IntelliJ APIs to Use

| Legacy API | Modern API |
|-----------|------------|
| `StdFileTypes.JAVA` | `JavaFileType.INSTANCE` |
| `StdFileTypes.HTML` | Use custom file type for TML |
| `DataKeys.PROJECT` | `CommonDataKeys.PROJECT` |
| Guava `Optional` | Java `Optional` |
| Guava collections | Java streams |

### 4.4 plugin.xml Structure

```xml
<idea-plugin>
    <id>com.juneenergy.intellij.tapestry5</id>
    <name>Tapestry 5.9 Support</name>
    <version>1.0.0</version>
    <vendor url="https://github.com/june-energy">June Energy</vendor>
    
    <description><![CDATA[
        Support for Apache Tapestry 5.9 web framework.
        Features:
        - Switch between Java and TML files
        - Code completion in TML templates
        - Navigation to Java from property expressions
        - Component reference resolution
    ]]></description>
    
    <depends>com.intellij.modules.platform</depends>
    <depends>com.intellij.modules.java</depends>
    <depends>com.intellij.modules.xml</depends>
    
    <extensions defaultExtensionNs="com.intellij">
        <!-- File type for .tml files (optional - XML based) -->
        
        <!-- Completion -->
        <completion.contributor 
            language="XML" 
            implementationClass="com.juneenergy.intellij.tapestry5.completion.Tapestry5CompletionContributor"/>
        
        <!-- Navigation -->
        <gotoDeclarationHandler 
            implementation="com.juneenergy.intellij.tapestry5.navigation.PropertyExpressionGoToHandler"/>
        <gotoDeclarationHandler 
            implementation="com.juneenergy.intellij.tapestry5.navigation.ComponentReferenceGoToHandler"/>
        
        <!-- Line Markers -->
        <codeInsight.lineMarkerProvider 
            language="JAVA"
            implementationClass="com.juneenergy.intellij.tapestry5.markers.Tapestry5LineMarkerProvider"/>
        
        <!-- Inspections -->
        <localInspection 
            language="JAVA"
            implementationClass="com.juneenergy.intellij.tapestry5.inspections.MissingTemplateInspection"/>
    </extensions>
    
    <actions>
        <action id="tapestry5.switcher" 
                class="com.juneenergy.intellij.tapestry5.Tapestry5Switcher"
                text="Switch Tapestry TML/Java Files"
                description="Switch between TML template and Java class">
            <add-to-group group-id="ToolsMenu" anchor="last"/>
            <keyboard-shortcut first-keystroke="control alt shift T" keymap="$default"/>
        </action>
    </actions>
</idea-plugin>
```

---

## Part 5: Migration Checklist

### From Legacy to Modern Plugin

- [ ] Remove Guava dependency (use Java 8+ features)
- [ ] Remove OGNL library (implement Tapestry 5 expression parser)
- [ ] Update to Java 17 syntax
- [ ] Replace deprecated IntelliJ APIs
- [ ] Add proper null-safety annotations
- [ ] Use modern notification APIs
- [ ] Implement proper threading (read/write actions)

### Files to Create (Minimum Viable Product)

1. **Build Configuration**
   - `build.gradle.kts`
   - `settings.gradle.kts`
   - `gradle.properties`
   - `gradle/libs.versions.toml`

2. **Plugin Configuration**
   - `src/main/resources/META-INF/plugin.xml`
   - `src/main/resources/icons/tapestry5.svg`

3. **Core Classes**
   - `Tapestry5Switcher.java` - File switching action
   - `Tapestry5FileUtils.java` - File type utilities
   - `Tapestry5Conventions.java` - Convention helpers
   - `TmlPartnerElementFinder.java` - Find Java from TML
   - `JavaPartnerElementFinder.java` - Find TML from Java

4. **Completion**
   - `Tapestry5CompletionContributor.java`

5. **Navigation**
   - `PropertyExpressionGoToHandler.java`

---

## Part 6: Timeline Estimate

| Phase | Estimated Duration |
|-------|-------------------|
| Phase 1: Project Setup | 1 day |
| Phase 2: Core Infrastructure | 1-2 days |
| Phase 3: File Switcher | 1 day |
| Phase 4: Code Completion | 2-3 days |
| Phase 5: Navigation | 2-3 days |
| Phase 6: References | 2 days |
| Phase 7: Inspections | 2 days |
| Phase 8: Line Markers | 1 day |
| Phase 9: Testing | 2-3 days |
| Phase 10: Documentation | 1 day |
| **Total** | **15-20 days** |

---

## Appendix: Useful Resources

- [IntelliJ Platform SDK Documentation](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- [Apache Tapestry 5 Documentation](https://tapestry.apache.org/documentation.html)
- [Tapestry 5 Component Reference](https://tapestry.apache.org/component-reference.html)
