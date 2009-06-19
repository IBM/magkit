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
import static org.apache.commons.lang.StringUtils.isNotBlank;
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

    /**
     * Sets the property at the given path. Creates a new property if it not already exists.
     * The path to the property must exist, otherwise the task will fail.
     */
    public static Child setProperty(final String path, final String value) {
        Child model;
        int lastIndex = path.lastIndexOf("/");
        if (lastIndex != -1) {
            String parentPath = path.substring(0, lastIndex);
            String name = path.substring(lastIndex + 1);
            model = select(parentPath, setProperty(name, value));
        } else {
            model = new PropertyModel(path, value);
        }
        return model;
    }

    /**
     * Removes the property at the given path. The path to the property must exist, otherwise the task will fail.
     */
    public static Child removeProperty(final String path) {
        Child model;
        int lastIndex = path.lastIndexOf("/");
        if (lastIndex != -1) {
            String parentPath = path.substring(0, lastIndex);
            String name = path.substring(lastIndex + 1);
            model = select(parentPath, remove(name));
        } else {
            model = new PropertyModel(path);
        }
        return model;
    }

    /**
     * Removes the property at the given path. The path to the property must exist, otherwise the task will fail.
     */
    public static Child removeProperties(final String path, final String... morePaths) {
        Child[] children = new Child[1 + (morePaths != null ? morePaths.length : 0)];
        children[0] = removeProperty(path);
        if (morePaths != null) {
            int index = 1;
            for (String morePath : morePaths) {
                children[index] = removeProperty(morePath);
            }
        }
        return select("", children);
    }

    /**
     * Creates new nodes if there are no existing. Different {@link ItemType}s will be ignored.
     */
    public static Child createNode(final String path, final Child... children) {
        return create(path, CONTENTNODE, children);
    }

    /**
     * Creates new nodes if there are no existing. Different {@link ItemType}s will be ignored.
     */
    public static Child createContent(final String path, final Child... children) {
        return create(path, CONTENT, children);
    }

    /**
     * Creates new nodes if there are no existing. Different {@link ItemType}s will be ignored.
     */
    public static Child create(final String path, final ItemType itemType, final Child... children) {
        return new NodeModel(path, NodeModel.Operation.create, itemType, children);
    }

    /**
     * Removes an existing node and all it successors. If there is no node at the given path nothing will be changed.
     */
    public static Child remove(final String path) {
        return new NodeModel(path, NodeModel.Operation.remove, null);
    }

    /**
     * Batch remove off existing nodes and all it successors.
     * If there is no node at the given path nothing will be changed.
     */
    public static Child remove(final String path, final String... morePaths) {
        Child[] children = new Child[1 + (morePaths != null ? morePaths.length : 0)];
        children[0] = remove(path);
        if (morePaths != null) {
            int index = 1;
            for (String morePath : morePaths) {
                children[index] = remove(morePath);
            }
        }
        return select("", children);
    }

    /**
     * Selects an existing node. The method defines the context for encapsulated operations. It will fail if there
     * is no node at the given path.
     */
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
    protected static final class PropertyModel implements Child {

        /**
         * Operations which can be performed with a node.
         */
        public enum Operation {
            set, remove
        }

        private final String _name;
        private final String _value;
        private final Operation _operation;

        private PropertyModel(final String name) {
            this(name, null, Operation.remove);
        }

        private PropertyModel(final String name, final String value) {
            this(name, value, Operation.set);
        }

        private PropertyModel(final String name, final String value, final Operation operation) {
            _name = name;
            _value = value;
            _operation = operation;
        }

        public void execute(final Content contextNode) throws RepositoryException {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("execute property model, node:" + contextNode.getHandle() + ", " + toString());
            }
            switch (_operation) {
                case remove:
                    if (contextNode.hasNodeData(_name)) {
                        NodeData property = contextNode.getNodeData(_name);
                        property.delete();
                    }
                    break;
                case set:
                    NodeData property = NodeDataUtil.getOrCreate(contextNode, _name);
                    String actualValue = property.getString();
                    // update value if not equal
                    if (!StringUtils.equals(_value, trim(actualValue))) {
                        property.setValue(_value);
                    }
                    break;
                default:
                    throw new IllegalStateException("unsupported operation:" + _operation);
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
    protected static final class NodeModel implements Child {
        private static final String PATH_SEPARATOR = "/";

        /**
         * Operations which can be performed with a node.
         */
        public enum Operation {
            select, create, remove
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
            String relativePath;
            if (_path.startsWith(PATH_SEPARATOR)) {
                // absolute path
                String workspaceName = contextNode.getWorkspace().getName();
                parentNode = getContent(workspaceName, "/");
                relativePath = _path.substring(1);
            } else {
                parentNode = contextNode;
                relativePath = _path;
            }
            Content node;
            switch (_operation) {
                case create:
                    // create only if not exists
                    node = createPath(parentNode, relativePath, _itemType);
                    break;
                case select:
                    if (isNotBlank(relativePath)) {
                        node = selectPath(parentNode, relativePath);
                        if (node == null) {
                            throw new RepositoryException("path does not exits, parent:" + parentNode.getHandle() + ", path:" + relativePath);
                        }
                    } else {
                        node = parentNode;
                    }
                    break;
                case remove:
                    node = selectPath(parentNode, relativePath);
                    if (node != null) {
                        node.delete();
                    }
                    break;
                default:
                    throw new IllegalStateException("unsupported operation:" + _operation);
            }
            if (_children != null && node != null) {
                for (Child child : _children) {
                    child.execute(node);
                }
            }
        }

        /**
         * Returns the node selected by given path. If path does not exists, returns {@code null}.
         */
        private Content selectPath(final Content parentNode, final String path) throws RepositoryException {
            Content node = parentNode;
            if (node != null) {
                for (String pathSegment : path.split("/")) {
                    if (!node.hasContent(pathSegment)) {
                        node = null;
                        break;
                    }
                    node = node.getContent(pathSegment);
                }
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