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
 * A task to bootstrap the installation files of a module.
 *
 * @author diana.racho (13.01.2011) Aperto AG
 */
public class InstallBootstrapTask extends BootstrapResourcesTask {

    public InstallBootstrapTask() {
        super("Bootstrap", "Bootstraps the necessary module repository content.");
    }

    /**
     * Accepts any resource under "/mgnl-bootstrap/install/moduleName" including any sub folders.
     */
    @Override
    protected boolean acceptResource(final InstallContext ctx, final String resourceName) {
        final String moduleName = ctx.getCurrentModuleDefinition().getName();
        return resourceName.startsWith("/mgnl-bootstrap/install/" + moduleName + "/") && super.acceptResource(ctx, resourceName);
    }
}
