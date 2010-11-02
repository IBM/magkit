package com.aperto.magkit.module;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.ModuleBootstrapTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

import java.util.List;

/**
 * A ModuleVersionHandler which just do the bootstrap on update.
 *
 * @author frank.sommer
 * @since 26.10.2010
 */
public class BootstrapModuleVersionHandler extends DefaultModuleVersionHandler {

    /**
     * this is used to bootstrap the new module-specific templates, dialogs ... .
     * bootstraps everything from "mgnl-bootstrap" folder.
     */
    private final Task _bootstrapModuleConfigTask = new ModuleBootstrapTask();

    /**
     * Constructor for adding update builder.
     */
    public BootstrapModuleVersionHandler() {
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(Version forVersion) {
        List<Task> updateTasks = super.getDefaultUpdateTasks(forVersion);
        updateTasks.add(_bootstrapModuleConfigTask);
        return updateTasks;
    }
}