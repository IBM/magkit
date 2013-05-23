package com.aperto.magkit.module;

import com.aperto.magkit.module.delta.CheckModuleServletsTask;
import com.aperto.magkit.module.delta.InstallBootstrapTask;
import com.aperto.magkit.module.delta.ModuleInstanceBootstrapTask;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.Task;
import info.magnolia.module.inplacetemplating.setup.TemplatesInstallTask;
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
    protected Delta getDefaultUpdate(final InstallContext installContext) {
        DeltaBuilder defaultUpdate = (DeltaBuilder) super.getDefaultUpdate(installContext);
        String moduleName = installContext.getCurrentModuleDefinition().getName();
        defaultUpdate.addTask(new IsModuleInstalledOrRegistered("Install FTLs for inplace editing", "Install all FTLs from modul to inplace templating repository.", "inplace-templating", new TemplatesInstallTask(".*/" + moduleName + "/.*\\.ftl", true)));
        return defaultUpdate;
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