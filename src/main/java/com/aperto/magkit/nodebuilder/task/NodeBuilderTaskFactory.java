package com.aperto.magkit.nodebuilder.task;

import static info.magnolia.repository.RepositoryConstants.CONFIG;

import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.task.ErrorHandling;

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
