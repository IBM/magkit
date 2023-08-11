package com.aperto.magkit.module.delta;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapResourcesTask;

/**
 * A task to bootstrap the install files of a module.
 *
 * @author diana.racho (13.01.2011) Aperto AG
 */
public class InstallBootstrapTask extends BootstrapResourcesTask {

    public InstallBootstrapTask() {
        super("Bootstrap", "Bootstraps the necessary module repository content.");
    }

    /**
     * Accepts any resource under "/mgnl-bootstrap/install/moduleName" including any subfolders.
     */
    @Override
    protected boolean acceptResource(final InstallContext ctx, final String resourceName) {
        final String moduleName = ctx.getCurrentModuleDefinition().getName();
        return resourceName.startsWith("/mgnl-bootstrap/install/" + moduleName + "/") && super.acceptResource(ctx, resourceName);
    }
}
