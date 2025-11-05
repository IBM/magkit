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
import info.magnolia.objectfactory.Components;
import org.apache.commons.io.FilenameUtils;

/**
 * Bootstrap task variant that loads module bootstrap resources conditionally based on instance type.
 * <p>
 * Accepts resources in any supported export format located directly under {@code /mgnl-bootstrap/<moduleName>/} as
 * well as those under instance-specific folders {@code /mgnl-bootstrap/author/<moduleName>/} or
 * {@code /mgnl-bootstrap/public/<moduleName>/}. This enables shipping author-only or public-only configuration/content.
 * </p>
 * <p>Preconditions: Server configuration must identify current instance role (admin/author vs public).</p>
 * <p>Side Effects: Imports repository content; author/public selective resources prevent leakage between environments.</p>
 * <p>Error Handling: Delegated to {@link ModuleBootstrapTask}; this class only filters resource selection.</p>
 * <p>Thread-Safety: No mutable state; safe for concurrent use.</p>
 * <p>Usage Example: Added from version handler during installation/update for environment-tailored bootstrapping.</p>
 *
 * @author diana.racho (Aperto AG)
 * @since 2011-01-13
 */
public class ModuleInstanceBootstrapTask extends ModuleBootstrapTask {

    /**
     * Determines acceptance of a resource by checking standard and instance-specific paths and supported extensions.
     *
     * @param ctx current install context
     * @param resourceName classpath resource name
     * @return true if resource belongs to generic or instance-specific bootstrap set
     */
    @Override
    protected boolean acceptResource(final InstallContext ctx, final String resourceName) {
        boolean acceptResources = super.acceptResource(ctx, resourceName);

        if (!acceptResources && acceptExtension(resourceName)) {
            String moduleName = ctx.getCurrentModuleDefinition().getName();

            if (Components.getComponent(ServerConfiguration.class).isAdmin()) {
                acceptResources = resourceName.startsWith("/mgnl-bootstrap/author/" + moduleName + "/");
            } else {
                acceptResources = resourceName.startsWith("/mgnl-bootstrap/public/" + moduleName + "/");
            }
        }

        return acceptResources;
    }

    /**
     * Checks if the resource file has a Magnolia-supported bootstrap extension.
     *
     * @param resourceName resource name
     * @return true if extension supported
     */
    protected boolean acceptExtension(final String resourceName) {
        return JcrExportCommand.Format.isSupportedExtension(FilenameUtils.getExtension(resourceName));
    }
}
