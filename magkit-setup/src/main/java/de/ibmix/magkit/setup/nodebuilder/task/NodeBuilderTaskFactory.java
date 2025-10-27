package de.ibmix.magkit.setup.nodebuilder.task;

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

import de.ibmix.magkit.assertions.Require;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.task.ErrorHandling;

import static info.magnolia.repository.RepositoryConstants.CONFIG;

/**
 * Factory for convenience creation of {@link NodeBuilderTask} instances targeting standard Magnolia configuration
 * locations (root, /server, /modules/<moduleName>). Provides a reduced parameter set with default logging error
 * handling and config workspace selection.
 * <p>Key features:
 * <ul>
 *     <li>Reduces boilerplate for common install/update tasks needing NodeBuilder operations.</li>
 *     <li>Standardizes use of {@link ErrorHandling#logging} to capture warnings during execution.</li>
 *     <li>Provides module-specific path construction under /modules.</li>
 * </ul>
 * Usage preconditions: Provided taskName and description should be non-null/meaningful; operations may be empty but
 * then the task performs no changes. Paths must exist; otherwise NodeBuilder operations may fail depending on their
 * own behavior. Side effects: Creates tasks that mutate JCR content during Magnolia install/update phases.
 * Thread-safety: All methods are stateless and static.
 *
 * @author wolf.bubenik
 * @since 2010-09-16
 */
public abstract class NodeBuilderTaskFactory {

    /**
     * Creates a NodeBuilderTask for the config workspace root path "/" using logging error handling.
     *
     * @param taskName name of the task
     * @param description human readable description
     * @param operations operations to execute at root path
     * @return configured task instance
     */
    public static NodeBuilderTask selectConfig(String taskName, String description, NodeOperation... operations) {
        return new NodeBuilderTask(taskName, description, ErrorHandling.logging, CONFIG, operations);
    }

    /**
     * Creates a NodeBuilderTask for the server configuration path "/server".
     *
     * @param taskName name of the task
     * @param description human readable description
     * @param operations operations to execute under /server
     * @return configured task instance
     */
    public static NodeBuilderTask selectServerConfig(String taskName, String description, NodeOperation... operations) {
        return new NodeBuilderTask(taskName, description, ErrorHandling.logging, CONFIG, "/server", operations);
    }

    /**
     * Creates a NodeBuilderTask for a specific module configuration path under /modules.
     *
     * @param taskName name of the task
     * @param description human readable description
     * @param moduleName module name appended to /modules/ to form root path; leading slash will be removed if present; must not be blank
     * @param operations operations to execute under /modules/<moduleName>
     * @return configured task instance
     * @throws IllegalArgumentException if moduleName is blank
     */
    public static NodeBuilderTask selectModuleConfig(String taskName, String description, String moduleName, NodeOperation... operations) {
        Require.Argument.notBlank(moduleName, "moduleName");
        String sanitizedModuleName = moduleName.startsWith("/") ? moduleName.substring(1) : moduleName;
        return new NodeBuilderTask(taskName, description, ErrorHandling.logging, CONFIG, "/modules/" + sanitizedModuleName, operations);
    }

    private NodeBuilderTaskFactory() {
    }
}
