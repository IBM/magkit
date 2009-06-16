package com.aperto.magkit.module.delta;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import static info.magnolia.cms.core.ItemType.CONTENT;
import static info.magnolia.cms.core.ItemType.CONTENTNODE;
import info.magnolia.cms.core.NodeData;
import static info.magnolia.cms.util.ContentUtil.getOrCreateContent;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.apache.commons.lang.StringUtils;
import static org.apache.commons.lang.StringUtils.trim;
import org.apache.log4j.Logger;

/**
 * A 'config' repository task to create tree structures with minimal afford of code.
 *
 * @author Norman Wiechmann, Aperto AG
 * @since 2009-03-17
 */
public class CreateConfigNodeTreeTask extends AbstractRepositoryTask {

    private static final Logger LOGGER = Logger.getLogger(CreateConfigNodeTreeTask.class);

    private final String _workspaceName = ContentRepository.CONFIG;
    private final String _parentPath;
    private ContentNodeModel _baseNode;

    protected CreateConfigNodeTreeTask(final String name, final String description, final String parentPath, final ContentNodeModel baseNode) {
        super(name, description);
        _parentPath = parentPath;
        _baseNode = baseNode;
    }

    /**
     * Returns a task that creates a content node within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     * <p/>
     * This method is the short cut of {@link #createConfigNode(String, String, String, String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])}
     * with auto generated task name and description.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENTNODE
     * @see #withProperty(String, String)
     * @see #withSubNode(String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static CreateConfigNodeTreeTask createConfigNode(final String parentPath, final String nodeName, final Child... children) {
        String taskName = "Create config node " + nodeName;
        String taskDescription = "Creates " + parentPath + "/" + nodeName + (children != null ? " and more." : ".");
        return createConfigNode(taskName, taskDescription, parentPath, nodeName, children);
    }

    /**
     * Returns a task that creates a content node within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENTNODE
     * @see #withProperty(String, String)
     * @see #withSubNode(String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static CreateConfigNodeTreeTask createConfigNode(final String taskName, final String taskDescription, final String parentPath, final String nodeName, final Child... children) {
        ContentNodeModel model = new ContentNodeModel(nodeName, true, CONTENTNODE, children);
        return new CreateConfigNodeTreeTask(taskName, taskDescription, parentPath, model);
    }

    /**
     * Returns a task that creates a content within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     * <p/>
     * This method is the short cut of {@link #createConfigContent(String, String, String, String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])}
     * with auto generated task name and description.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENT
     * @see #withProperty(String, String)
     * @see #withSubNode(String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static CreateConfigNodeTreeTask createConfigContent(final String parentPath, final String nodeName, final Child... children) {
        String taskName = "Create config content " + nodeName;
        String taskDescription = "Creates " + parentPath + "/" + nodeName + (children != null ? " and more." : ".");
        return createConfigContent(taskName, taskDescription, parentPath, nodeName, children);
    }

    /**
     * Returns a task that creates a content within the 'config' repository below the given path.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     *
     * @see info.magnolia.cms.core.ItemType#CONTENT
     * @see #withProperty(String, String)
     * @see #withSubNode(String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static CreateConfigNodeTreeTask createConfigContent(final String taskName, final String taskDescription, final String parentPath, final String nodeName, final Child... children) {
        ContentNodeModel model = new ContentNodeModel(nodeName, true, CONTENT, children);
        return new CreateConfigNodeTreeTask(taskName, taskDescription, parentPath, model);
    }

    /**
     * Returns a task that creates a content node within the 'config' repository below the given path.
     * The new node may have properties and nodes wich may have themself properties and nodes and so on.
     * <p/>
     * This method is the short cut of {@link #goToConfigNode(String, String, String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])}
     * with auto generated task name and description.
     *
     * @see #addProperty(String, String)
     * @see #addNode(String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static CreateConfigNodeTreeTask goToConfigNode(final String path, final Child... children) {
        String taskName = "Manipulate " + path;
        String taskDescription = "Manipulate " + path + (children != null ? " and more." : ".");
        return goToConfigNode(taskName, taskDescription, path, children);
    }

    /**
     * Returns a task that creates a content node within the 'config' repository below the given path.
     * The new node may have properties and nodes wich may have themself properties and nodes and so on.
     *
     * @see #addProperty(String, String)
     * @see #addNode(String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static CreateConfigNodeTreeTask goToConfigNode(final String taskName, final String taskDescription, final String path, final Child... children) {
        int lastPathSeparator = path.lastIndexOf("/");
        if (lastPathSeparator == -1) {
            throw new IllegalArgumentException("Path must contain at least one path separator '/'.");
        }
        String parentPath = path.substring(0, lastPathSeparator);
        String nodeName = path.substring(lastPathSeparator + 1);
        ContentNodeModel model = new ContentNodeModel(nodeName, false, CONTENTNODE, children);
        return new CreateConfigNodeTreeTask(taskName, taskDescription, parentPath, model);
    }

    /**
     * Returns a property to add to a node declaration.
     *
     * @see #createConfigNode(String, String, String, String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static PropertyModel withProperty(final String name, final String value) {
        return addProperty(name, value);
    }

    /**
     * Returns a property to add to a node declaration.
     *
     * @see #createConfigNode(String, String, String, String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static PropertyModel addProperty(final String name, final String value) {
        return new PropertyModel(name, value);
    }

    /**
     * Returns a sub node to add to a node declaration.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     *
     * @see #createConfigNode(String, String, String, String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static ContentNodeModel withSubNode(final String name, final Child... children) {
        return addNode(name, children);
    }

    /**
     * Returns a sub node to add to a node declaration.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     *
     * @see #createConfigNode(String, String, String, String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static ContentNodeModel addNode(final String name, final Child... children) {
        return new ContentNodeModel(name, true, CONTENTNODE, children);
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        HierarchyManager hm = installContext.getHierarchyManager(_workspaceName);
        Content parentNode = hm.getContent(_parentPath);
        createSubNode(parentNode, _baseNode);
        parentNode.save();
    }

    protected void createProperties(final Content node, final PropertyModel[] properties) throws RepositoryException {
        for (PropertyModel property : properties) {
            createProperty(node, property.getName(), property.getValue());
        }
    }

    protected void createProperty(final Content node, final String propertyName, final String newValue) throws RepositoryException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("create property, node handle:" + node.getHandle() + ", property name:" + propertyName + ", value:" + newValue);
        }
        NodeData property = NodeDataUtil.getOrCreate(node, propertyName);
        String actualValue = property.getString();
        if (!StringUtils.equals(newValue, trim(actualValue))) {
            property.setValue(newValue);
        }
    }

    protected void createSubNodes(final Content parentNode, final ContentNodeModel[] contentNodeModels) throws RepositoryException {
        for (ContentNodeModel model : contentNodeModels) {
            createSubNode(parentNode, model);
        }
    }

    protected void createSubNode(final Content parentNode, final ContentNodeModel model) throws RepositoryException {
        String name = model.getName();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("create sub node, parent handle:" + parentNode.getHandle() + ", node name:" + name);
        }
        Content node = model.isCreateNode() ?
            getOrCreateContent(parentNode, name, model.getItemType()) : parentNode.getContent(name);
        createProperties(node, model.getProperties());
        createSubNodes(node, model.getSubNodes());
    }

    /**
     * Corporate interface of {@link PropertyModel} and {@link ContentNodeModel}.
     */
    public interface Child {
        // no interface defintion
    }

    /**
     * A data holder containing the data needed to create a property node.
     */
    protected static class PropertyModel implements Child {
        private final String _name;
        private final String _value;

        protected PropertyModel(final String name, final String value) {
            _name = name;
            _value = value;
        }

        public String getName() {
            return _name;
        }

        public String getValue() {
            return _value;
        }
    }

    /**
     * A data holder containing the data needed to create a content node.
     */
    public static class ContentNodeModel implements Child {
        private final String _name;
        private final PropertyModel[] _properties;
        private final ContentNodeModel[] _subNodes;
        private final boolean _createNode;
        private final ItemType _itemType;

        protected ContentNodeModel(final String name, final boolean createNode, final ItemType itemType, final Child... children) {
            _name = name;
            _createNode = createNode;
            _itemType = itemType;
            _properties = extractProperties(children);
            _subNodes = extractSubNodes(children);
        }

        protected static PropertyModel[] extractProperties(final Child... children) {
            List<PropertyModel> properties = new ArrayList<PropertyModel>();
            for (Child child : children) {
                if (child instanceof PropertyModel) {
                    properties.add((PropertyModel) child);
                }
            }
            return properties.toArray(new PropertyModel[properties.size()]);
        }

        protected static ContentNodeModel[] extractSubNodes(final Child... children) {
            List<ContentNodeModel> subNodes = new ArrayList<ContentNodeModel>();
            for (Child child : children) {
                if (child instanceof ContentNodeModel) {
                    subNodes.add((ContentNodeModel) child);
                }
            }
            return subNodes.toArray(new ContentNodeModel[subNodes.size()]);
        }

        public String getName() {
            return _name;
        }

        public boolean isCreateNode() {
            return _createNode;
        }

        public ItemType getItemType() {
            return _itemType;
        }

        public PropertyModel[] getProperties() {
            return _properties;
        }

        public ContentNodeModel[] getSubNodes() {
            return _subNodes;
        }
    }
}