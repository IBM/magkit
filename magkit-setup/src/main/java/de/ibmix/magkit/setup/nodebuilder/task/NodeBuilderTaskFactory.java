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

import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.task.ErrorHandling;

import static info.magnolia.repository.RepositoryConstants.CONFIG;

/**
 * A task using the NodeBuilder API, applying operations on a given path.
 * Added some factory static methods.
 *
 * @author wolf.bubenik
 * @since 16.09.2010
 */
public abstract class NodeBuilderTaskFactory {

    /**
     * Creates a NodeBuilderTask with ErrorHandling.logging and RepositoryConstants.CONFIG.
     *
     * @param taskName    The name of the task
     * @param description A description
     * @param operations  A list of operations to be performed on the config repository root node (path: /)
     * @return the new NodeBuilderTask instance
     */
    public static NodeBuilderTask selectConfig(String taskName, String description, NodeOperation... operations) {
        return new NodeBuilderTask(taskName, description, ErrorHandling.logging, CONFIG, operations);
    }

    /**
     * Creates a NodeBuilderTask with ErrorHandling.logging and RepositoryConstants.CONFIG for the server config node.
     *
     * @param taskName    The name of the task
     * @param description A description
     * @param operations  A list of operations to be performed on the server config repository root node (path: /server)
     * @return the new NodeBuilderTask instance
     */
    public static NodeBuilderTask selectServerConfig(String taskName, String description, NodeOperation... operations) {
        return new NodeBuilderTask(taskName, description, ErrorHandling.logging, CONFIG, "/server", operations);
    }

    /**
     * Creates a NodeBuilderTask with ErrorHandling.logging and RepositoryConstants.CONFIG for the config node of the named module.
     *
     * @param taskName    The name of the task
     * @param description A description
     * @param moduleName  The name of the module that should be configurated
     * @param operations  A list of operations to be performed on the module config repository root node (path: /modules/moduleName)
     * @return the new NodeBuilderTask instance
     */
    public static NodeBuilderTask selectModuleConfig(String taskName, String description, String moduleName, NodeOperation... operations) {
        return new NodeBuilderTask(taskName, description, ErrorHandling.logging, CONFIG, "/modules/" + moduleName, operations);
    }

    private NodeBuilderTaskFactory() {
    }
}
