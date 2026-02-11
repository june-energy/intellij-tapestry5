/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.juneenergy.intellij.tapestry5.util.Tapestry5FileUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listens for TML file changes and triggers a rebuild of the containing module.
 *
 * <p>When a TML (Tapestry Markup Language) file is modified and saved, this listener
 * triggers a rebuild of the module containing the file. This ensures that the
 * Tapestry page can be properly reloaded with the updated template.
 *
 * <p>The listener is registered as a project-level message bus subscriber for
 * VFS (Virtual File System) events.
 *
 * @author June Energy
 * @since 1.1.0
 */
public class TmlFileChangeListener implements BulkFileListener {

    private static final Logger LOG = Logger.getInstance(TmlFileChangeListener.class);

    private final Project project;

    /**
     * Creates a new TML file change listener for the given project.
     *
     * @param project the project to monitor for TML file changes
     */
    public TmlFileChangeListener(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        if (project.isDisposed()) {
            return;
        }

        Set<Module> modulesToRebuild = new HashSet<>();

        for (VFileEvent event : events) {
            if (event instanceof VFileContentChangeEvent) {
                VirtualFile file = event.getFile();
                if (file != null && Tapestry5FileUtils.isTmlFile(file)) {
                    Module module = ModuleUtilCore.findModuleForFile(file, project);
                    if (module != null && !module.isDisposed()) {
                        modulesToRebuild.add(module);
                        LOG.debug("TML file changed: " + file.getPath() + " in module: " + module.getName());
                    }
                }
            }
        }

        if (!modulesToRebuild.isEmpty()) {
            rebuildModules(modulesToRebuild);
        }
    }

    /**
     * Triggers a rebuild of the specified modules.
     *
     * @param modules the modules to rebuild
     */
    private void rebuildModules(@NotNull Set<Module> modules) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }

            CompilerManager compilerManager = CompilerManager.getInstance(project);
            Module[] moduleArray = modules.toArray(new Module[0]);

            LOG.info("Triggering rebuild for " + modules.size() + " module(s) due to TML file changes");

            compilerManager.make(
                compilerManager.createModulesCompileScope(moduleArray, false),
                new CompileStatusNotification() {
                    @Override
                    public void finished(boolean aborted, int errors, int warnings, @NotNull CompileContext compileContext) {
                        if (aborted) {
                            LOG.debug("Module rebuild was aborted");
                        } else if (errors > 0) {
                            LOG.debug("Module rebuild completed with " + errors + " error(s)");
                        } else {
                            LOG.debug("Module rebuild completed successfully");
                        }
                    }
                }
            );
        });
    }
}
