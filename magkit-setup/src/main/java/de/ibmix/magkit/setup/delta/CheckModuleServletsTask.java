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

import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.RegisterServletTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.objectfactory.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.removeIfExists;
import static de.ibmix.magkit.setup.nodebuilder.task.NodeBuilderTaskFactory.selectServerConfig;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;

/**
 * Checks and (re)registers all servlet definitions declared by the current module.
 * <p>
 * The task iterates over the {@link ServletDefinition} list of the {@link ModuleDefinition} currently being installed
 * and generates a sequence of sub tasks: for each existing servlet configuration a removal task is added first to
 * ensure a clean re-installation, followed by a {@link RegisterServletTask} to register the servlet with a validated
 * node name. Finally all accumulated sub tasks are executed in order by {@link ArrayDelegateTask#execute(InstallContext)}.
 * </p>
 * <p>
 * Preconditions: A valid config workspace session must be obtainable from the {@link InstallContext}. If repository
 * access fails the task logs and warns but continues without registering additional tasks.
 * </p>
 * <p>
 * Side Effects: Removes existing servlet configuration nodes below /server/filters/servlets prior to re-registration.
 * </p>
 * <p>
 * Error Handling: Repository access issues are caught and converted into an install warning; no exception is thrown
 * upwards to keep the installation resilient.
 * </p>
 * <p>
 * Thread-Safety: Intended to run in Magnolia's single-threaded module installation phase; no special synchronization.
 * </p>
 * Usage Example: Automatically part of a module version handler assembling installation/update tasks.
 *
 * @author frank.sommer
 * @since 2011-06-14
 */
public class CheckModuleServletsTask extends ArrayDelegateTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckModuleServletsTask.class);
    private static final String DEFAULT_RELATIVE_PATH = "filters/servlets";
    private static final String DEFAULT_ABSOLUTE_PATH = "/server/" + DEFAULT_RELATIVE_PATH;

    public CheckModuleServletsTask() {
        super("Register module servlets", "Registers servlets for this module.");
    }

    /**
     * Builds and executes the sub task list for servlet re-registration.
     * <p>
     * For each servlet definition: if a node with the servlet name already exists it adds a removal task first, then
     * adds a {@link RegisterServletTask}. After collection, delegates execution to the super implementation.
     * </p>
     *
     * @param installContext current installation context supplying module definition and repository sessions
     * @throws TaskExecutionException if a delegated sub task throws an exception during its execution
     */
    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        final ModuleDefinition moduleDefinition = installContext.getCurrentModuleDefinition();
        String errorMessage = "Unable to access workspace config to check on servlet definitions for module '" + moduleDefinition.getName() + "'.";

        try {
            Session session = installContext.getConfigJCRSession();
            NodeNameHelper nodeNameHelper = Components.getComponent(NodeNameHelper.class);

            // iterate module servlets
            for (ServletDefinition servletDefinition : moduleDefinition.getServlets()) {
                String servletName = servletDefinition.getName();

                // add remove task for existing servlets before adding a register task
                if (session.itemExists(DEFAULT_ABSOLUTE_PATH + "/" + servletName)) {
                    addTask(
                        selectServerConfig("Remove servlet configuration", "Remove servlet configuration '" + servletName + "'.",
                            getNode(DEFAULT_RELATIVE_PATH).then(removeIfExists(servletName))
                        )
                    );
                }
                addTask(new RegisterServletTask(servletDefinition, nodeNameHelper));
            }
        } catch (RepositoryException e) {
            installContext.warn(errorMessage);
            LOGGER.info(errorMessage, e);
        }

        super.execute(installContext);
    }
}
