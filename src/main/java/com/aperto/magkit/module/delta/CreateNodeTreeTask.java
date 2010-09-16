package com.aperto.magkit.module.delta;

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import static info.magnolia.cms.core.ItemType.CONTENT;
import static info.magnolia.cms.core.ItemType.CONTENTNODE;
import info.magnolia.cms.core.NodeData;
import static info.magnolia.cms.util.ContentUtil.createPath;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import org.apache.commons.lang.StringUtils;
import static org.apache.commons.lang.StringUtils.*;
import org.apache.log4j.Logger;

/**
 * A 'config' repository task to create tree structures with minimal afford of code.
 *
 * @author Norman Wiechmann, Aperto AG
 * @since 2009-03-17
 * @deprecated Use magnolia nodebuilder API and com.aperto.magkit.nodebuilder.NodeOperationFactory
 */
public class CreateNodeTreeTask extends AbstractRepositoryTask {

    private static final Logger LOGGER = Logger.getLogger(CreateNodeTreeTask.class);
    private static final String PATH_SEPARATOR = "/";

    private final String _workspaceName;
    private final Child _model;

    protected CreateNodeTreeTask(final String name, final String description, final String workspaceName, final Child model) {
        super(name, description);
        _workspaceName = workspaceName;
        _model = model;
    }

    public static Task selectWorkspace(final String workspaceName, final String path, final Child... children) {
        String taskName = "update configuration below " + path;
        String taskDescription = "manipulates " + path + (children != null ? " and more." : ".");
        return new CreateNodeTreeTask(taskName, taskDescription, workspaceName, select(path, children));
    }

    public static Task selectWorkspace(final String name, final String description, final String workspaceName, final String path, final Child... children) {
        return new CreateNodeTreeTask(name, description, workspaceName, select(path, children));
    }

    /**
     * Sets the property at the given path. Creates a new property if it not already exists.
     * The path to the property must exist, otherwise the task will fail.
     */
    public static Child setProperty(final String path, final String value) {
        Child model;
        String normalizedPath = normalizePath(path);
        int lastIndex = normalizedPath.lastIndexOf(PATH_SEPARATOR);
        if (lastIndex != -1) {
            String parentPath = normalizedPath.substring(0, lastIndex);
            String name = normalizedPath.substring(lastIndex + 1);
            model = select(parentPath, setProperty(name, value));
        } else {
            model = new PropertyModel(normalizedPath, value);
        }
        return model;
    }

    /**
     * Removes the property at the given path. The path to the property must exist, otherwise the task will fail.
     */
    public static Child removeProperty(final String path) {
        Child model;
        String normalizedPath = normalizePath(path);
        int lastIndex = normalizedPath.lastIndexOf(PATH_SEPARATOR);
        if (lastIndex != -1) {
            String parentPath = normalizedPath.substring(0, lastIndex);
            String name = normalizedPath.substring(lastIndex + 1);
            model = select(parentPath, remove(name));
        } else {
            model = new PropertyModel(normalizedPath);
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
     * Selects an existing node. The method defines the context for encapsulated operations. It will not fail directly
     * if there is no node at the given path. But subsequent operation which require an existing node, like create a
     * node or set a property, will fail.
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
        _model.execute(repositoryRoot, PATH_SEPARATOR);
        repositoryRoot.save();
    }

    private static String normalizePath(final String path) {
        return removeEnd(removeStart(trimToEmpty(path), PATH_SEPARATOR), PATH_SEPARATOR);
    }

    /**
     * Corporate interface of {@link PropertyModel} and {@link CreateNodeTreeTask.NodeModel}.
     */
    public interface Child {
        void execute(final Content contextNode, final String contextPath) throws RepositoryException;
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

        public void execute(final Content contextNode, final String contextPath) throws RepositoryException {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("context:" + contextPath + ", " + toString());
            }
            switch (_operation) {
                case remove:
                    if (contextNode != null && contextNode.hasNodeData(_name)) {
                        NodeData property = contextNode.getNodeData(_name);
                        property.delete();
                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("nothing to delete, path:" + contextPath + ", " + toString());
                        }
                    }
                    break;
                case set:
                    if (contextNode == null) {
                        throw new RepositoryException(
                            "operation failed - path does not exits, context:" + contextPath + ", " + toString());
                    }
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
                "_name='" + _name + '\'' +
                ", _operation=" + _operation +
                ", _value='" + _value + '\'' +
                '}';
        }
    }

    /**
     * A data holder containing the data needed to select or create a repository node.
     */
    protected static final class NodeModel implements Child {

        /**
         * Operations which can be performed with a node.
         */
        public enum Operation {
            select, create, remove
        }

        private final String _relativePath;
        private final Operation _operation;
        private final Child[] _children;
        private final ItemType _itemType;

        private NodeModel(final String relativePath, final Child... children) {
            this(relativePath, Operation.select, null, children);
        }

        private NodeModel(final String relativePath, final Operation operation, final ItemType itemType, final Child... children) {
            _relativePath = removeEnd(removeStart(relativePath, PATH_SEPARATOR), PATH_SEPARATOR);
            _operation = operation;
            _itemType = itemType;
            _children = children;
        }

        public void execute(final Content contextNode, final String contextPath) throws RepositoryException {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("context:" + contextPath + ", " + toString());
            }
            Content newContextNode;
            String newContextPath;
            switch (_operation) {
                case create:
                    if (contextNode == null) {
                        throw new RepositoryException(
                            "operation failed - path does not exits, context:" + contextPath + ", " + toString());
                    }
                    // create only if not exists
                    newContextNode = createPath(contextNode, _relativePath, _itemType);
                    newContextPath = newContextNode.getHandle();
                    break;
                case select:
                    if (isNotBlank(_relativePath)) {
                        newContextNode = selectPath(contextNode, _relativePath);
                        newContextPath = (contextPath.endsWith(PATH_SEPARATOR) ?
                            contextPath : contextPath + PATH_SEPARATOR) + _relativePath;
                    } else {
                        newContextNode = contextNode;
                        newContextPath = contextPath;
                    }
                    break;
                case remove:
                    newContextNode = selectPath(contextNode, _relativePath);
                    newContextPath = contextPath;
                    if (newContextNode != null) {
                        newContextNode.delete();
                    }
                    break;
                default:
                    throw new IllegalStateException("unsupported operation:" + _operation);
            }
            if (_children != null) {
                for (Child child : _children) {
                    if (child != null) {
                        child.execute(newContextNode, newContextPath);
                    }
                }
            }
        }

        /**
         * Returns the node selected by given path. If path does not exists, returns {@code null}.
         */
        private Content selectPath(final Content parentNode, final String path) throws RepositoryException {
            Content node = parentNode;
            if (node != null) {
                for (String pathSegment : path.split(PATH_SEPARATOR)) {
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
            return "NodeModel{" +
                "_relativePath='" + _relativePath + '\'' +
                ", _operation=" + _operation +
                ", _itemType=" + _itemType +
                '}';
        }
    }
}