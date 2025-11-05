package de.ibmix.magkit.setup;

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

import de.ibmix.magkit.setup.delta.CheckModuleServletsTask;
import de.ibmix.magkit.setup.delta.InstallBootstrapTask;
import de.ibmix.magkit.setup.delta.ModuleInstanceBootstrapTask;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

import java.util.ArrayList;
import java.util.List;

import static de.ibmix.magkit.setup.delta.StandardTasks.hasModuleNewRevision;

/**
 * ModuleVersionHandler responsible for bootstrapping module configuration and registering module-specific servlets.
 * <p>
 * Purpose: Orchestrates Magnolia update and install lifecycle steps for this module by providing additional tasks
 * that ensure configuration (templates, dialogs, config nodes) and servlet registrations are present after
 * installation or update.
 * </p>
 * <p>Main functionalities / key features:</p>
 * <ul>
 *   <li>Adds a complete bootstrap of module configuration on install (all files under <code>/mgnl-bootstrap/install/moduleName</code>).</li>
 *   <li>Adds incremental bootstrap of updated configuration on module updates when the module revision changes.</li>
 *   <li>Ensures all module-provided servlets are registered if missing.</li>
 * </ul>
 *
 * <p>Important details:</p>
 * <ul>
 *   <li>Revision detection is delegated to {@link de.ibmix.magkit.setup.delta.StandardTasks#hasModuleNewRevision(Version, Version)}.</li>
 *   <li>Returned task and delta lists are never {@code null}; Magnolia processes them sequentially.</li>
 *   <li>Errors or exceptions thrown by underlying tasks are handled by Magnolia's installation framework.</li>
 * </ul>
 *
 * <p><strong>Usage preconditions:</strong> Magnolia will instantiate and invoke this handler automatically when declared in the module descriptor (typically <code>module.xml</code>). Manual instantiation is rarely required.</p>
 * <p><strong>Side effects:</strong> Creates or updates JCR configuration nodes and may register servlet definitions.</p>
 * <p><strong>Thread-safety:</strong> Not designed for concurrent use; Magnolia invokes version handlers in a single-threaded install/update context.</p>
 * <p><strong>Example:</strong></p>
 * <pre>
 * // In module descriptor (module.xml):
 * &lt;versionHandler&gt;de.ibmix.magkit.setup.BootstrapModuleVersionHandler&lt;/versionHandler&gt;;
 * </pre>
 *
 * @author frank.sommer
 * @since 2010-10-26
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
     * Builds update deltas adding the default update delta when the module revision has changed.
     * @param installContext current installation context provided by Magnolia; never {@code null}
     * @param from the version currently installed; never {@code null}
     * @return list of deltas to apply for update; never {@code null}
     */
    @Override
    protected List<Delta> getUpdateDeltas(final InstallContext installContext, final Version from) {
        List<Delta> updateDeltas = super.getUpdateDeltas(installContext, from);
        final Version toVersion = installContext.getCurrentModuleDefinition().getVersion();

        if (hasModuleNewRevision(from, toVersion)) {
            updateDeltas.add(getDefaultUpdate(installContext));
        }

        return updateDeltas;
    }

    /**
     * Provides default update tasks and appends module configuration bootstrap and servlet registration tasks.
     * @param forVersion the target version being updated to; never {@code null}
     * @return list of tasks executed during a default update; never {@code null}
     */
    @Override
    protected List<Task> getDefaultUpdateTasks(Version forVersion) {
        List<Task> updateTasks = super.getDefaultUpdateTasks(forVersion);
        updateTasks.add(_bootstrapModuleConfigTask);
        updateTasks.add(_checkServletRegistrationTask);
        return updateTasks;
    }

    /**
     * Provides additional install tasks by adding a full bootstrap of all module configuration.
     * @param installContext current installation context; never {@code null}
     * @return list of extra tasks executed during module install; never {@code null}
     */
    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> installTasks = new ArrayList<>(super.getExtraInstallTasks(installContext));
        installTasks.add(new InstallBootstrapTask());
        return installTasks;
    }
}
