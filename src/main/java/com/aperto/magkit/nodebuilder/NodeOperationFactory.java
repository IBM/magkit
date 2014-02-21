package com.aperto.magkit.nodebuilder;

import info.magnolia.jcr.nodebuilder.AbstractNodeOperation;
import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.Ops;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

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
     * New Operation to solve problems with dublicating of nodes on repeated updates.
     * Creates node with given name, if it does not exist allready.
     * If such node exists, the existing node will be returned.
     *
     * @param name the node name as String
     * @return the new or existing node with the given name
     */
    public static NodeOperation addOrGetNode(final String name) {
        return addOrGetNode(name, null);
    }

    /**
     * New Operation to solve problems with dublicating of nodes on repeated updates.
     * Creates node with given name and nodetype {@link info.magnolia.jcr.util.NodeTypes.ContentNode#NAME},
     * if it does not exist allready.
     * If such node exists, the existing node will be returned.
     *
     * @param name the node name as String
     * @return the new or existing node with the given name
     */
    public static NodeOperation addOrGetContentNode(final String name) {
        return addOrGetNode(name, NodeTypes.ContentNode.NAME);
    }

    /**
     * New Operation to solve problems with dublicating of nodes on repeated updates.
     * Creates node with given name, if it does not exist allready.
     * If such node exists, the existing node will be returned.
     *
     * @param name the node name as String
     * @return the new or existing node with the given name
     */
    public static NodeOperation addOrGetNode(final String name, final String type) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                Node result;
                if (context.hasNode(name)) {
                    result = context.getNode(name);
                    if (type != null && !result.isNodeType(type)) {
                        throw new RepositoryException("Type of Node '" + name + "' does not match. Expected: " + type + "   found : " + result.getPrimaryNodeType().getName());
                    }
                } else {
                    result = context.addNode(name, type);
                }
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
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
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
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                final Value value = PropertyUtil.createValue(newValue, context.getSession().getValueFactory());
                context.setProperty(name, value);
                return context;
            }
        };
    }

    /**
     * Checks the name before try to delete.
     */
    public static NodeOperation removeIfExists(final String name) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                if (context.hasProperty(name) || context.hasNode(name)) {
                    context.getSession().removeItem(context.getPath() + PATH_SEPARATOR + name);
                }
                return context;
            }
        };
    }

    private NodeOperationFactory() {
    }
}
