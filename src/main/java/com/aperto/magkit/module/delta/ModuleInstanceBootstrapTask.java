package com.aperto.magkit.module.delta;

import com.google.inject.Inject;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ModuleBootstrapTask;

/**
 * A task to bootstrap the bootstrap files of a module depending on the current instance.
 *
 * @author diana.racho (Aperto AG)
 */
public class ModuleInstanceBootstrapTask extends ModuleBootstrapTask {

    @Inject
    private ServerConfiguration _serverConfig;

    /**
     * Accepts any resource under "/mgnl-bootstrap/moduleName" including any subfolders.
     * And accepts on author instance any resources under "/mgnl-bootstrap/author/moduleName"
     * and accepts on public instance any resources under "/mgnl-bootstrap/public/moduleName".
     */
    protected boolean acceptResource(InstallContext ctx, String resourceName) {
        boolean acceptResources = super.acceptResource(ctx, resourceName);
        final String moduleName = ctx.getCurrentModuleDefinition().getName();
        if (!acceptResources) {
            if (_serverConfig.isAdmin()) {
                acceptResources = resourceName.startsWith("/mgnl-bootstrap/author/" + moduleName + "/") && resourceName.endsWith(".xml");
            } else {
                acceptResources = resourceName.startsWith("/mgnl-bootstrap/public/" + moduleName + "/") && resourceName.endsWith(".xml");
            }
        }
        return acceptResources;
    }
}
