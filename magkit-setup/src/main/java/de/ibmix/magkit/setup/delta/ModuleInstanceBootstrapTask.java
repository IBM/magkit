package de.ibmix.magkit.setup.delta;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
