package com.aperto.magkit.module.delta;

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import static info.magnolia.cms.core.ItemType.CONTENT;
import static info.magnolia.cms.core.ItemType.CONTENTNODE;
import info.magnolia.cms.core.NodeData;
import static info.magnolia.cms.util.ContentUtil.createPath;
import static info.magnolia.cms.util.ContentUtil.getContent;
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
public class AbstractCreateNodeTreeTask extends AbstractRepositoryTask {

    private static final Logger LOGGER = Logger.getLogger(AbstractCreateNodeTreeTask.class);

    private final String _workspaceName;
    private final Child _model;

    protected AbstractCreateNodeTreeTask(final String name, final String description, final String workspaceName, final Child model) {
        super(name, description);
        _workspaceName = workspaceName;
        _model = model;
    }

    public static Child withProperty(final String name, final String value) {
        return setProperty(name, value);
    }

    public static Child setProperty(final String name, final String value) {
        return new PropertyModel(name, value);
    }

    public static Child withSubNode(final String path, final Child... children) {
        return addNode(path, children);
    }

    public static Child withSubContent(final String path, final Child... children) {
        return addContent(path, children);
    }

    public static Child with(final String path, final ItemType itemType, final Child... children) {
        return add(path, itemType, children);
    }

    public static Child addNode(final String path, final Child... children) {
        return new NodeModel(path, NodeModel.Operation.create, CONTENTNODE, children);
    }

    public static Child addContent(final String path, final Child... children) {
        return new NodeModel(path, NodeModel.Operation.create, CONTENT, children);
    }

    public static Child add(final String path, final ItemType itemType, final Child... children) {
        return new NodeModel(path, NodeModel.Operation.create, itemType, children);
    }

    public static Child replaceNode(final String path, final Child... children) {
        return new NodeModel(path, NodeModel.Operation.replace, CONTENTNODE, children);
    }

    public static Child replaceContent(final String path, final Child... children) {
        return new NodeModel(path, NodeModel.Operation.replace, CONTENT, children);
    }

    public static Child replaceNode(final String path, final ItemType itemType, final Child... children) {
        return new NodeModel(path, NodeModel.Operation.replace, itemType, children);
    }

    public static Child select(final String path, final Child... children) {
        return new NodeModel(path, children);
    }

    /**
     * Implementation of the abstract method of {@link AbstractRepositoryTask}.
     */
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        HierarchyManager hm = installContext.getHierarchyManager(_workspaceName);
        Content repositoryRoot = hm.getRoot();
        _model.execute(repositoryRoot);
        repositoryRoot.save();
    }

    /**
     * Corporate interface of {@link PropertyModel} and {@link com.aperto.magkit.module.delta.AbstractCreateNodeTreeTask.NodeModel}.
     */
    public interface Child {
        void execute(final Content contextNode) throws RepositoryException;
    }

    /**
     * A data holder containing the data needed to create a property node.
     */
    private static final class PropertyModel implements Child {
        private final String _name;
        private final String _value;

        private PropertyModel(final String name, final String value) {
            _name = name;
            _value = value;
        }

        public void execute(final Content contextNode) throws RepositoryException {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("execute property model, node:" + contextNode.getHandle() + ", " + toString());
            }
            NodeData property = NodeDataUtil.getOrCreate(contextNode, _name);
            String actualValue = property.getString();
            // update value if not equal
            if (!StringUtils.equals(_value, trim(actualValue))) {
                property.setValue(_value);
            }
        }

        @Override
        public String toString() {
            return "PropertyModel{" +
                "name='" + _name + '\'' +
                ", value='" + _value + '\'' +
                '}';
        }
    }

    /**
     * A data holder containing the data needed to select or create a repository node.
     */
    private static final class NodeModel implements Child {
        private static final String PATH_SEPARATOR = "/";

        /**
         *
         */
        public enum Operation {
            select, create, replace
        }

        private final String _path;
        private final Operation _operation;
        private final Child[] _children;
        private final ItemType _itemType;

        private NodeModel(final String path, final Child... children) {
            this(path, Operation.select, null, children);
        }

        private NodeModel(final String path, final Operation operation, final ItemType itemType, final Child... children) {
            _path = path;
            _operation = operation;
            _itemType = itemType;
            _children = children;
        }

        public void execute(final Content contextNode) throws RepositoryException {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("execute node model, node:" + contextNode.getHandle() + ", " + toString());
            }
            Content parentNode;
            if (_path.startsWith(PATH_SEPARATOR)) {
                String workspaceName = contextNode.getWorkspace().getName();
                parentNode = getContent(workspaceName, "/");
            } else {
                parentNode = contextNode;
            }
            Content node;
            switch (_operation) {
                case create:
                    node = createPath(parentNode, _path, _itemType);
                    break;
                case replace:
                    try {
                        node = selectPath(parentNode, _path);
                        node.delete();
                    } catch (RepositoryException e) {
                        // nothing to delete, path not exists
                    }
                    node = createPath(parentNode, _path, _itemType);
                    break;
                case select:
                    node = selectPath(parentNode, _path);
                    break;
                default:
                    throw new IllegalStateException("unsupported operation:" + _operation);
            }
            for (Child child : _children) {
                child.execute(node);
            }
        }

        private Content selectPath(final Content parentNode, final String path) throws RepositoryException {
            Content node = parentNode;
            for (String pathSegment : path.split("/")) {
                node = node.getContent(pathSegment);
            }
            return node;
        }

        @Override
        public String toString() {
            return "ContentNodeModel{" +
                "_path='" + _path + '\'' +
                ", _itemType=" + _itemType +
                '}';
        }
    }
}