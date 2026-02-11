/*
 * Tapestry 5.9 IntelliJ Plugin
 *
 * Copyright (c) 2024 June Energy
 * Licensed under the Apache License, Version 2.0
 */
package com.juneenergy.intellij.tapestry5.listeners;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.messages.MessageBusConnection;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Startup activity that registers the TML file change listener when a project opens.
 *
 * <p>This activity is automatically executed when a project is opened. It subscribes
 * the {@link TmlFileChangeListener} to the VFS (Virtual File System) events message bus
 * to monitor for TML file changes.
 *
 * @author June Energy
 * @since 1.1.0
 */
public class TmlFileChangeStartupActivity implements ProjectActivity {

    private static final Logger LOG = Logger.getInstance(TmlFileChangeStartupActivity.class);

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        LOG.info("Registering TML file change listener for project: " + project.getName());

        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new TmlFileChangeListener(project));

        LOG.debug("TML file change listener registered successfully");
        
        return Unit.INSTANCE;
    }
}
