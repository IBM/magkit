package com.aperto.magkit.module.delta;

import info.magnolia.cms.beans.config.ContentRepository;

/**
 * A task to create tree structures with minimal afford of code.
 *
 * @author Norman Wiechmann, Aperto AG
 * @since 2009-03-17
 * @deprecated Use magnolia nodebuilder API and com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory 
 */
public class CreateConfigNodeTreeTask extends CreateNodeTreeTask {

    protected CreateConfigNodeTreeTask(final String name, final String description, final Child model) {
        super(name, description, ContentRepository.CONFIG, model);
    }

    /**
     * Returns a task that selects a node within the 'config' repository below the given path.
     * The node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     * <p/>
     * This method is the short cut of {@link #selectConfig(String, String, String, Child[])}
     * with auto generated task name and description.
     */
    public static CreateConfigNodeTreeTask selectConfig(final String path, final Child... children) {
        String taskName = "update configuration below " + path;
        String taskDescription = "manipulates " + path + (children != null ? " and more." : ".");
        return selectConfig(taskName, taskDescription, path, children);
    }

    /**
     * Returns a task that selects a node within the 'config' repository below the given path.
     * The node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     */
    public static CreateConfigNodeTreeTask selectConfig(final String taskName, final String taskDescription, final String path, final Child... children) {
        return new CreateConfigNodeTreeTask(taskName, taskDescription, select(path, children));
    }
}