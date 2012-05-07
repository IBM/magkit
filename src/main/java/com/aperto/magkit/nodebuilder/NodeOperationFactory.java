package com.aperto.magkit.nodebuilder;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.nodebuilder.ErrorHandler;
import info.magnolia.nodebuilder.NodeOperation;
import info.magnolia.nodebuilder.Ops;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import static info.magnolia.cms.core.MgnlNodeType.NT_CONTENT;
import static info.magnolia.cms.util.ContentUtil.createPath;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.removeEnd;

/**
 * An Utility class that extends info.magnolia.nodebuilder.Ops.
 * It provides additional methods for getOrCreate operations and ordering operations.
 *
 * @author wolf.bubenik
 * @since 16.09.2010
 */
public abstract class NodeOperationFactory extends Ops {
    public static final String PATH_SEPARATOR = "/";

    /**
     * New Operation to solve problems with dublicating of Content on repeated updates.
     * Creates content with given name, if it does not exist allready.
     * If such content exists, the existing content will be returned.
     *
     * @param name the content name as String
     * @return the new or existing content with the given name
     */
    public static NodeOperation addOrGetNode(final String name) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                Content result;
                if (context.hasContent(name)) {
                    result = context.getContent(name);
                } else {
                    result = context.createContent(name);
                }
                return result;
            }
        };
    }

    /**
     * New Operation to solve problems with dublicating of Content on repeated updates.
     * Creates content with given name, if it does not exist allready.
     * If such content exists, the existing content will be returned.
     *
     * @param name the content name as String
     * @return the new or existing content with the given name
     */
    public static NodeOperation addOrGetNode(final String name, final String type) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                Content result;
                if (context.hasContent(name)) {
                    result = context.getContent(name);
                    if (!equalsIgnoreCase(type, result.getNodeTypeName())) {
                        throw new RepositoryException("Type of Node '" + name + "' does not match. Expected: " + type + "   found : " + result.getNodeTypeName());
                    }
                } else {
                    result = context.createContent(name, type);
                }
                return result;
            }
        };
    }

    public static NodeOperation createContentPath(final String relativePath) {
        return createItemTypePath(relativePath, ItemType.CONTENT);
    }

    public static NodeOperation createContentNodePath(final String relativePath) {
        return createItemTypePath(relativePath, ItemType.CONTENTNODE);
    }

    public static NodeOperation createItemTypePath(final String relativePath, final ItemType type) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                String path = removeEnd(relativePath.trim(), PATH_SEPARATOR);
                Content result = context;
                result = createPath(context, path, type);
                return result;
            }
        };
    }

    /**
     * Moves the named node before its sibbling.
     *
     * @param nodeName            the name of the node to be moved
     * @param orderBeforeNodeName the name of the node sibbing that should be ordered behind the named node
     * @return the NodeOperation performing the ordering operation
     */
    public static NodeOperation orderBefore(final String nodeName, final String orderBeforeNodeName) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                context.getParent().orderBefore(nodeName, orderBeforeNodeName);
                return context;
            }
        };
    }

    /**
     * Sets the value of an existing property, ignoring its current value.
     * If the property does not exist it will be created. No Exception will be thrown.
     * This reflects the behaviour of the internaly called method setNodeDate(..) that replaces the deprecated method createNodeData(..).
     *
     * @param name     the name of the node to be moved
     * @param newValue the name of the node sibbing that should be ordered behind the named node
     * @return the NodeOperation performing the operation
     */
    public static NodeOperation addOrSetProperty(final String name, final Object newValue) {
        return new AbstractOp() {
            Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                final Value value = NodeDataUtil.createValue(newValue, context.getJCRNode().getSession().getValueFactory());
                context.setNodeData(name, value);
                return context;
            }
        };
    }


    /**
     * Default implementation. Copied from superclass because it is package private there.
     */
    abstract static class AbstractOp implements NodeOperation {
        private NodeOperation[] _childrenOps = {};

        public void exec(Content context, ErrorHandler errorHandler) {
            Content transfomed = null;
            try {
                transfomed = doExec(context, errorHandler);
            } catch (RepositoryException e) {
                errorHandler.handle(e, context);
            }

            for (NodeOperation childrenOp : _childrenOps) {
                childrenOp.exec(transfomed, errorHandler);
            }
        }

        /**
         * @return the node that should now be used as the context for subsequent operations
         */
        abstract Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException;

        public NodeOperation then(NodeOperation... childrenOps) {
            _childrenOps = childrenOps;
            return this;
        }
    }

    private NodeOperationFactory() {
    }
}
