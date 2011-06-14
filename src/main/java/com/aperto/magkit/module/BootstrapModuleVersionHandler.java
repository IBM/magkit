package com.aperto.magkit.module;

import com.aperto.magkit.module.delta.*;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

import java.util.ArrayList;
import java.util.List;

/**
 * A ModuleVersionHandler which just do the bootstrap on update and bootstraps on module install all bootstrap files under "/mgnl-bootstrap/install/moduleName".
 *
 * @author frank.sommer
 * @since 26.10.2010
 */
public class BootstrapModuleVersionHandler extends DefaultModuleVersionHandler {

    /**
     * this is used to bootstrap the new module-specific templates, dialogs ... .
     * bootstraps everything from "mgnl-bootstrap" folder.
     */
    private final Task _bootstrapModuleConfigTask = new ModuleInstanceBootstrapTask();

    /**
     * Registers all unregistered servlets.
     */
    private final Task _checkServletRegistrationTask = new CheckModuleServletsTask();

    /**
     * Constructor for adding update builder.
     */
    public BootstrapModuleVersionHandler() {
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(Version forVersion) {
        List<Task> updateTasks = super.getDefaultUpdateTasks(forVersion);
        updateTasks.add(_bootstrapModuleConfigTask);
        updateTasks.add(_checkServletRegistrationTask);
        return updateTasks;
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> installTasks = new ArrayList<Task>();
        installTasks.addAll(super.getExtraInstallTasks(installContext));
        installTasks.add(new InstallBootstrapTask());
        return installTasks;
    }
}