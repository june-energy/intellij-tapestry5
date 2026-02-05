/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

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
        // TODO: Phase 3 - Implement file switching logic
        // 1. Get current file from editor
        // 2. Determine file type (Java or TML)
        // 3. Find partner file using conventions
        // 4. Navigate to partner file or show notification
    }

    /**
     * Updates the action's enabled state based on the current context.
     *
     * <p>The action is enabled only when:
     * <ul>
     *     <li>A project is open</li>
     *     <li>An editor is active</li>
     *     <li>The current file is either a Java file or a TML file</li>
     * </ul>
     *
     * @param e the action event containing context information
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        // TODO: Phase 3 - Implement action availability logic
        // For now, always enable the action
        e.getPresentation().setEnabledAndVisible(true);
    }
}
