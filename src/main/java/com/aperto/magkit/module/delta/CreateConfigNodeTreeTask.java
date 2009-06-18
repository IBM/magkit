package com.aperto.magkit.module.delta;

import info.magnolia.cms.beans.config.ContentRepository;

/**
 * A task to create tree structures with minimal afford of code.
 *
 * @author Norman Wiechmann, Aperto AG
 * @since 2009-03-17
 */
public class CreateConfigNodeTreeTask extends AbstractCreateNodeTreeTask {

    protected CreateConfigNodeTreeTask(final String name, final String description, final Child model) {
        super(name, description, ContentRepository.CONFIG, model);
    }

    /**
     * Returns a task that creates a content node within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     * <p/>
     * This method is the short cut of {@link #createConfigNode(String, String, String, Child[])}
     * with auto generated task name and description.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENTNODE
     */
    public static CreateConfigNodeTreeTask createConfigNode(final String path, final Child... children) {
        String taskName = "update configuration below " + path;
        String taskDescription = "manipulates " + path + (children != null ? " and more." : ".");
        return createConfigNode(taskName, taskDescription, path, children);
    }

    /**
     * Returns a task that creates a content node within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENTNODE
     */
    public static CreateConfigNodeTreeTask createConfigNode(final String taskName, final String taskDescription, final String path, final Child... children) {
        return new CreateConfigNodeTreeTask(taskName, taskDescription, addNode(path, children));
    }

    /**
     * Returns a task that creates a content node within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENTNODE
     * @deprecated Use {@link #createConfigNode(String, String, String, Child[])} instead.
     */
    public static CreateConfigNodeTreeTask createConfigNode(final String taskName, final String taskDescription, final String parentPath, final String nodeName, final Child... children) {
        String path = parentPath + "/" + nodeName;
        return new CreateConfigNodeTreeTask(taskName, taskDescription, addNode(path, children));
    }

    /**
     * Returns a task that replaces a content node within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     * <p/>
     * This method is the short cut of {@link #replaceConfigNode(String, String, String, Child[])}
     * with auto generated task name and description.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENTNODE
     */
    public static CreateConfigNodeTreeTask replaceConfigNode(final String path, final Child... children) {
        String taskName = "update configuration below " + path;
        String taskDescription = "manipulates " + path + (children != null ? " and more." : ".");
        return replaceConfigNode(taskName, taskDescription, path, children);
    }

    /**
     * Returns a task that replaces a content node within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENTNODE
     */
    public static CreateConfigNodeTreeTask replaceConfigNode(final String taskName, final String taskDescription, final String path, final Child... children) {
        return new CreateConfigNodeTreeTask(taskName, taskDescription, replaceNode(path, children));
    }

    /**
     * Returns a task that creates a content within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     * <p/>
     * This method is the short cut of {@link #createConfigContent(String, String, String, Child[])}
     * with auto generated task name and description.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENT
     */
    public static CreateConfigNodeTreeTask createConfigContent(final String path, final Child... children) {
        String taskName = "update configuration below " + path;
        String taskDescription = "manipulates " + path + (children != null ? " and more." : ".");
        return createConfigContent(taskName, taskDescription, path, children);
    }

    /**
     * Returns a task that creates a content within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENT
     */
    public static CreateConfigNodeTreeTask createConfigContent(final String taskName, final String taskDescription, final String path, final Child... children) {
        return new CreateConfigNodeTreeTask(taskName, taskDescription, addContent(path, children));
    }

    /**
     * Returns a task that replaces a content within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     * <p/>
     * This method is the short cut of {@link #replaceConfigContent(String, String, String, Child[])}
     * with auto generated task name and description.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENT
     */
    public static CreateConfigNodeTreeTask replaceConfigContent(final String path, final Child... children) {
        String taskName = "update configuration below " + path;
        String taskDescription = "manipulates " + path + (children != null ? " and more." : ".");
        return replaceConfigContent(taskName, taskDescription, path, children);
    }

    /**
     * Returns a task that replaces a content within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENT
     */
    public static CreateConfigNodeTreeTask replaceConfigContent(final String taskName, final String taskDescription, final String path, final Child... children) {
        return new CreateConfigNodeTreeTask(taskName, taskDescription, replaceContent(path, children));
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