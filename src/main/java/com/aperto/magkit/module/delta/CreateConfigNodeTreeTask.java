package com.aperto.magkit.module.delta;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import static info.magnolia.cms.core.ItemType.CONTENTNODE;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.apache.commons.lang.StringUtils;
import static org.apache.commons.lang.StringUtils.trim;

/**
 * A 'config' repository task to create tree structures with minimal afford of code.
 *
 * @author Norman Wiechmann, Aperto AG
 * @since 2009-03-17
 */
public class CreateConfigNodeTreeTask extends AbstractRepositoryTask {

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
     *
     * @see #withProperty(String, String)
     * @see #withSubNode(String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static CreateConfigNodeTreeTask createConfigNode(final String taskName, final String taskDescription, final String parentPath, final String nodeName, final Child... children) {
        return new CreateConfigNodeTreeTask(taskName, taskDescription, parentPath, new ContentNodeModel(nodeName, children));
    }

    /**
     * Returns a property to add to a node declaration.
     *
     * @see #createConfigNode(String, String, String, String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static PropertyModel withProperty(final String name, final String value) {
        return new PropertyModel(name, value);
    }

    /**
     * Returns a sub node to add to a node declaration.
     * The new node may have properties and sub nodes wich may have themself properties and sub nodes and so on.
     *
     * @see #createConfigNode(String, String, String, String, com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.Child[])
     */
    public static ContentNodeModel withSubNode(final String name, final Child... children) {
        return new ContentNodeModel(name, children);
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
        Content node = ContentUtil.getOrCreateContent(parentNode, model.getName(), CONTENTNODE);
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
    public static class PropertyModel implements Child {
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

        protected ContentNodeModel(final String name, final Child... children) {
            _name = name;
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

        public PropertyModel[] getProperties() {
            return _properties;
        }

        public ContentNodeModel[] getSubNodes() {
            return _subNodes;
        }
    }
}