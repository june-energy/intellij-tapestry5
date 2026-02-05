/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.juneenergy.intellij.tapestry5.util.Tapestry5FileUtils;
import com.juneenergy.intellij.tapestry5.util.Tapestry5PluginHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Action to switch between Tapestry TML template files and their corresponding Java class files.
 *
 * <p>This action provides a quick way to navigate between:
 * <ul>
 *     <li>Java page/component class → TML template</li>
 *     <li>TML template → Java page/component class</li>
 * </ul>
 *
 * <p>The action uses Tapestry 5.x conventions to find partner files:
 * <ul>
 *     <li>Template files have the same name as the Java class with .tml extension</li>
 *     <li>Templates are located in the same package as their Java class</li>
 * </ul>
 *
 * <p>Keyboard shortcut: Ctrl+Alt+Shift+T (configurable)
 *
 * @author June Energy
 * @since 1.0.0
 * @see <a href="https://tapestry.apache.org/component-templates.html">Tapestry Component Templates</a>
 */
public class Tapestry5Switcher extends AnAction {

    /**
     * Performs the file switch action.
     *
     * <p>When invoked on a Java file, attempts to navigate to the corresponding TML template.
     * When invoked on a TML file, attempts to navigate to the corresponding Java class.
     *
     * @param e the action event containing context information
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        VirtualFile currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (currentFile == null) {
            return;
        }

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        
        if (Tapestry5FileUtils.isJavaFile(currentFile)) {
            // Switch from Java to TML
            switchToTml(project, psiFile);
        } else if (Tapestry5FileUtils.isTmlFile(currentFile)) {
            // Switch from TML to Java
            switchToJava(project, currentFile);
        } else {
            Tapestry5PluginHelper.showWarning(project, 
                    "Current file is not a Java or TML file");
        }
    }

    /**
     * Switches from a Java file to its corresponding TML template.
     *
     * @param project the current project
     * @param psiFile the current Java PSI file
     */
    private void switchToTml(@NotNull Project project, @Nullable PsiFile psiFile) {
        if (!(psiFile instanceof PsiJavaFile javaFile)) {
            Tapestry5PluginHelper.showWarning(project, "Not a valid Java file");
            return;
        }

        PsiClass mainClass = Tapestry5PluginHelper.getMainClass(javaFile);
        if (mainClass == null) {
            Tapestry5PluginHelper.showWarning(project, "No class found in file");
            return;
        }

        VirtualFile tmlFile = Tapestry5PluginHelper.findTmlForClass(project, mainClass);
        if (tmlFile != null) {
            Tapestry5PluginHelper.openFile(project, tmlFile);
            Tapestry5PluginHelper.showInfo(project, 
                    "Switched to template: " + tmlFile.getName());
        } else {
            String expectedName = Tapestry5FileUtils.getTmlFileNameForClass(mainClass);
            Tapestry5PluginHelper.showWarning(project, 
                    "No TML template found for " + mainClass.getName() + 
                    ". Expected: " + expectedName);
        }
    }

    /**
     * Switches from a TML template to its corresponding Java class.
     *
     * @param project the current project
     * @param tmlFile the current TML file
     */
    private void switchToJava(@NotNull Project project, @NotNull VirtualFile tmlFile) {
        PsiClass javaClass = Tapestry5PluginHelper.findClassForTml(project, tmlFile);
        
        if (javaClass != null) {
            javaClass.navigate(true);
            Tapestry5PluginHelper.showInfo(project, 
                    "Switched to class: " + javaClass.getName());
        } else {
            String expectedClassName = Tapestry5FileUtils.getClassNameForTmlFile(tmlFile);
            Tapestry5PluginHelper.showWarning(project, 
                    "No Java class found for " + tmlFile.getName() + 
                    ". Expected class: " + expectedClassName);
        }
    }

    /**
     * Updates the action's enabled state based on the current context.
     *
     * <p>The action is enabled only when:
     * <ul>
     *     <li>A project is open</li>
     *     <li>A file is currently open in the editor</li>
     *     <li>The current file is either a Java file or a TML file</li>
     * </ul>
     *
     * @param e the action event containing context information
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        
        boolean enabled = project != null && file != null && 
                (Tapestry5FileUtils.isJavaFile(file) || Tapestry5FileUtils.isTmlFile(file));
        
        e.getPresentation().setEnabledAndVisible(enabled);
    }
    
    /**
     * Returns the action ID used for fast-path updates.
     *
     * @return {@code ActionUpdateThread.BGT} for background thread updates
     */
    @Override
    public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
        return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
    }
}
