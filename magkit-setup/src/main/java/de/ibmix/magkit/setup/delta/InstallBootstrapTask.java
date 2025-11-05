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

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapResourcesTask;

/**
 * Task that bootstraps all installation resources of the current module.
 * <p>
 * It filters resources located under {@code /mgnl-bootstrap/install/<moduleName>/} (including sub folders) and delegates
 * actual bootstrapping to {@link BootstrapResourcesTask}. Both XML and YAML resources supported by Magnolia's export
 * mechanism are accepted; other file types are rejected by the super implementation.
 * </p>
 * <p>Preconditions: Resources must be packaged under the installation path named after the module.</p>
 * <p>Side Effects: Imports repository content into the respective workspaces defined by the resource files.</p>
 * <p>Error Handling: Relies on {@link BootstrapResourcesTask} for exception handling; this class only narrows resource selection.</p>
 * <p>Thread-Safety: Executed during single-threaded module installation.</p>
 * <p>Usage Example: Added from a module version handler to provision initial configuration/content.</p>
 *
 * @author diana.racho Aperto AG
 * @since 2011-01-13
 */
public class InstallBootstrapTask extends BootstrapResourcesTask {

    public InstallBootstrapTask() {
        super("Bootstrap", "Bootstraps the necessary module repository content.");
    }

    /**
     * Determines whether a resource should be bootstrapped.
     * <p>
     * Accepts any resource residing under the module-specific installation directory. Delegates further validation
     * (like file extension) to the super class.
     * </p>
     *
     * @param ctx current install context (used to obtain module name)
     * @param resourceName classpath resource name
     * @return true if resource is in the installation directory and accepted by super class
     */
    @Override
    protected boolean acceptResource(final InstallContext ctx, final String resourceName) {
        final String moduleName = ctx.getCurrentModuleDefinition().getName();
        return resourceName.startsWith("/mgnl-bootstrap/install/" + moduleName + "/") && super.acceptResource(ctx, resourceName);
    }
}
