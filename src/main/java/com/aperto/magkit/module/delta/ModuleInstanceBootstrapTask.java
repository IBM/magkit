package com.aperto.magkit.module.delta;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.importexport.command.JcrExportCommand;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ModuleBootstrapTask;
import org.apache.commons.io.FilenameUtils;

/**
 * A task to bootstrap the bootstrap files of a module depending on the current instance.
 *
 * @author diana.racho (Aperto AG)
 */
public class ModuleInstanceBootstrapTask extends ModuleBootstrapTask {

    /**
     * Accepts any resource under "/mgnl-bootstrap/moduleName" including any subfolders.
     * And accepts on author instance any resources under "/mgnl-bootstrap/author/moduleName"
     * and accepts on public instance any resources under "/mgnl-bootstrap/public/moduleName".
     */
    protected boolean acceptResource(final InstallContext ctx, final String resourceName) {
        boolean acceptResources = super.acceptResource(ctx, resourceName);

        if (!acceptResources && acceptExtension(resourceName)) {
            String moduleName = ctx.getCurrentModuleDefinition().getName();

            if (ServerConfiguration.getInstance().isAdmin()) {
                acceptResources = resourceName.startsWith("/mgnl-bootstrap/author/" + moduleName + "/");
            } else {
                acceptResources = resourceName.startsWith("/mgnl-bootstrap/public/" + moduleName + "/");
            }
        }

        return acceptResources;
    }

    protected boolean acceptExtension(final String resourceName) {
        return JcrExportCommand.Format.isSupportedExtension(FilenameUtils.getExtension(resourceName));
    }
}
