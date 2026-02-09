/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.inspections;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.juneenergy.intellij.tapestry5.util.Tapestry5Conventions;
import com.juneenergy.intellij.tapestry5.util.Tapestry5FileUtils;
import com.juneenergy.intellij.tapestry5.util.Tapestry5PluginHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Inspection that reports Tapestry page or component classes that don't have
 * a corresponding TML (Tapestry Markup Language) template file.
 *
 * <p>This inspection checks all Tapestry element classes (pages, components, mixins)
 * and reports a warning if no corresponding TML template file is found in the
 * expected location (same package in resources).
 *
 * <p>A quick fix is provided to create the missing template file with a basic
 * HTML5 structure including the Tapestry namespace.
 *
 * @author June Energy
 * @since 1.0.0
 * @see Tapestry5Conventions
 */
public class MissingTemplateInspection extends AbstractBaseJavaLocalInspectionTool {

    /**
     * Creates a visitor to check Java classes for missing TML templates.
     *
     * @param holder the holder for problems found
     * @param isOnTheFly whether inspection is running on-the-fly
     * @return the PSI element visitor
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitClass(@NotNull PsiClass aClass) {
                // Skip anonymous, local, and inner classes
                if (aClass.getName() == null || 
                    aClass.getContainingClass() != null ||
                    aClass.getQualifiedName() == null) {
                    return;
                }

                // Check if this is a Tapestry element (page, component, or mixin)
                Tapestry5Conventions.TapestryElementType elementType = 
                    Tapestry5Conventions.detectElementType(aClass);

                if (elementType == Tapestry5Conventions.TapestryElementType.NONE) {
                    return;
                }

                // Skip mixins - they typically don't have templates
                if (elementType == Tapestry5Conventions.TapestryElementType.MIXIN) {
                    return;
                }

                // Check if a corresponding TML template exists
                Project project = aClass.getProject();
                VirtualFile tmlFile = Tapestry5PluginHelper.findTmlForClass(project, aClass);

                if (tmlFile == null) {
                    // No template found - register the problem
                    String elementTypeName = elementType == Tapestry5Conventions.TapestryElementType.PAGE 
                        ? "page" : "component";
                    String message = String.format(
                        "Tapestry %s '%s' has no corresponding TML template",
                        elementTypeName, aClass.getName());

                    // Register problem on the class name identifier
                    if (aClass.getNameIdentifier() != null) {
                        holder.registerProblem(
                            aClass.getNameIdentifier(),
                            message,
                            new CreateTemplateQuickFix(aClass.getName(), 
                                aClass.getQualifiedName(), elementType)
                        );
                    }
                }
            }
        };
    }

    /**
     * Quick fix that creates a TML template file for a Tapestry class.
     */
    private static class CreateTemplateQuickFix implements LocalQuickFix {
        private final String className;
        private final String qualifiedName;
        private final Tapestry5Conventions.TapestryElementType elementType;

        CreateTemplateQuickFix(String className, String qualifiedName, 
                               Tapestry5Conventions.TapestryElementType elementType) {
            this.className = className;
            this.qualifiedName = qualifiedName;
            this.elementType = elementType;
        }

        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return "Create TML template";
        }

        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getName() {
            return "Create TML template '" + className + ".tml'";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiFile containingFile = descriptor.getPsiElement().getContainingFile();
            if (containingFile == null) {
                return;
            }

            VirtualFile javaFile = containingFile.getVirtualFile();
            if (javaFile == null) {
                return;
            }

            // Find the resources directory that corresponds to the java source directory
            VirtualFile resourcesDir = findResourcesDirectory(javaFile);
            if (resourcesDir == null) {
                Tapestry5PluginHelper.showWarning(project, 
                    "Could not locate resources directory to create template");
                return;
            }

            // Create the template content
            String templateContent = generateTemplateContent();

            // Calculate the target directory path
            String packagePath = Tapestry5Conventions.getPackageName(qualifiedName)
                .replace('.', '/');

            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    // Create or find the package directory in resources
                    VirtualFile targetDir = createOrFindDirectory(resourcesDir, packagePath);
                    if (targetDir == null) {
                        ApplicationManager.getApplication().invokeLater(() ->
                            Tapestry5PluginHelper.showError(project, 
                                "Failed to create directory structure for template"));
                        return;
                    }

                    // Check if file already exists
                    String templateFileName = className + "." + Tapestry5FileUtils.TML_EXTENSION;
                    if (targetDir.findChild(templateFileName) != null) {
                        ApplicationManager.getApplication().invokeLater(() ->
                            Tapestry5PluginHelper.showWarning(project, 
                                "Template file already exists: " + templateFileName));
                        return;
                    }

                    // Create the PsiFile
                    PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
                    PsiFile tmlFile = fileFactory.createFileFromText(
                        templateFileName,
                        XmlFileType.INSTANCE,
                        templateContent
                    );

                    // Add the file to the directory
                    PsiDirectory psiTargetDir = containingFile.getManager()
                        .findDirectory(targetDir);
                    if (psiTargetDir != null) {
                        PsiFile addedFile = (PsiFile) psiTargetDir.add(tmlFile);
                        
                        // Open the newly created file
                        VirtualFile newVirtualFile = addedFile.getVirtualFile();
                        if (newVirtualFile != null) {
                            ApplicationManager.getApplication().invokeLater(() -> {
                                FileEditorManager.getInstance(project)
                                    .openFile(newVirtualFile, true);
                                Tapestry5PluginHelper.showInfo(project, 
                                    "Created template: " + templateFileName);
                            });
                        }
                    }
                } catch (Exception e) {
                    ApplicationManager.getApplication().invokeLater(() ->
                        Tapestry5PluginHelper.showError(project, 
                            "Failed to create template: " + e.getMessage()));
                }
            });
        }

        /**
         * Finds the resources directory corresponding to a Java source file.
         */
        private VirtualFile findResourcesDirectory(VirtualFile javaFile) {
            String path = javaFile.getPath();
            
            // Look for standard Maven/Gradle source structure
            int srcMainJavaIndex = path.indexOf("/src/main/java/");
            if (srcMainJavaIndex >= 0) {
                String basePath = path.substring(0, srcMainJavaIndex);
                VirtualFile baseDir = javaFile.getFileSystem()
                    .findFileByPath(basePath + "/src/main/resources");
                if (baseDir != null) {
                    return baseDir;
                }
            }

            // Try src/test structure
            int srcTestJavaIndex = path.indexOf("/src/test/java/");
            if (srcTestJavaIndex >= 0) {
                String basePath = path.substring(0, srcTestJavaIndex);
                VirtualFile baseDir = javaFile.getFileSystem()
                    .findFileByPath(basePath + "/src/test/resources");
                if (baseDir != null) {
                    return baseDir;
                }
            }

            return null;
        }

        /**
         * Creates or finds a directory path starting from a base directory.
         */
        private VirtualFile createOrFindDirectory(VirtualFile baseDir, String path) {
            if (path.isEmpty()) {
                return baseDir;
            }

            String[] parts = path.split("/");
            VirtualFile current = baseDir;

            try {
                for (String part : parts) {
                    if (part.isEmpty()) {
                        continue;
                    }
                    VirtualFile child = current.findChild(part);
                    if (child == null) {
                        child = current.createChildDirectory(this, part);
                    }
                    current = child;
                }
                return current;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * Generates the template content based on the element type.
         */
        private String generateTemplateContent() {
            String typeName = elementType == Tapestry5Conventions.TapestryElementType.PAGE
                ? "Page" : "Component";

            return "<!DOCTYPE html>\n" +
                "<html xmlns:t=\"http://tapestry.apache.org/schema/tapestry_5_4.xsd\"\n" +
                "      xmlns:p=\"tapestry:parameter\">\n" +
                "<head>\n" +
                "    <title>" + className + "</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <!-- " + typeName + ": " + className + " -->\n" +
                "    <h1>" + className + "</h1>\n" +
                "\n" +
                "    <!-- Your content here -->\n" +
                "\n" +
                "</body>\n" +
                "</html>\n";
        }
    }
}
