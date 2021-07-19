package com.aperto.magkit.nodebuilder;

import info.magnolia.jcr.nodebuilder.AbstractNodeOperation;
import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.Ops;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.voting.voters.URIPatternVoter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import static com.aperto.magkit.module.delta.StandardTasks.PN_CLASS;
import static com.aperto.magkit.module.delta.StandardTasks.PN_PATTERN;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.strip;

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
     * @param relPath the node name or relative node path as String
     * @return the new or existing node with the given name
     */
    public static NodeOperation addOrGetContentNode(final String relPath) {
        return addOrGetNode(relPath, NodeTypes.ContentNode.NAME);
    }

    /**
     * New Operation to solve problems with dublicating of nodes on repeated updates.
     * Creates nodes with given name (relPath).
     * @see CreatePathNodeOperation
     *
     * @param relPath the node name or relative node path as String
     * @return NodeOperation with created node.
     */
    public static NodeOperation addOrGetNode(final String relPath, final String type) {
        return new CreatePathNodeOperation(relPath, type);
    }

    /**
     * Moves the named node before its sibling.
     * Wrapper for {@link javax.jcr.Node#orderBefore(String, String)}
     *
     * @param nodeName            the name of the node to be moved
     * @param orderBeforeNodeName the name of the node sibling that should be ordered behind the named node
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
     * Moves the named node after its sibling.
     * Wrapper for {@link info.magnolia.jcr.util.NodeUtil#orderAfter(Node, String)}
     *
     * @param nodeName            the name of the node to be moved
     * @param orderAfterNodeName the name of the node sibling that should be ordered after the named node
     * @return the NodeOperation performing the ordering operation
     */
    public static NodeOperation orderAfter(final String nodeName, final String orderAfterNodeName) {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                NodeUtil.orderAfter(context.getNode(nodeName), orderAfterNodeName);
                return context;
            }
        };
    }

    /**
     * Sets the value of an existing property, ignoring its current value.
     * If the property does not exist it will be created. No Exception will be thrown.
     *
     * @param name     the name of the node to be moved
     * @param newValue the name of the node sibling that should be ordered behind the named node
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
                    context.getSession().removeItem(removeEnd(context.getPath(), PATH_SEPARATOR) + PATH_SEPARATOR + name);
                }
                return context;
            }
        };
    }

    /**
     * Removes all child nodes.
     */
    public static NodeOperation removeAllChilds() {
        return new AbstractNodeOperation() {
            @Override
            protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                Session session = context.getSession();
                String parentPath = removeEnd(context.getPath(), PATH_SEPARATOR) + PATH_SEPARATOR;
                NodeIterator nodes = context.getNodes();
                while (nodes.hasNext()) {
                    Node nodeToRemove = nodes.nextNode();
                    session.removeItem(parentPath + nodeToRemove.getName());
                }
                return context;
            }
        };
    }

    /**
     * Creates an uri pattern voter node operation.
     */
    public static NodeOperation addUriPatternVoter(final String voterName, final String pattern) {
        return addPatternVoter(voterName, URIPatternVoter.class.getName(), pattern);
    }

    /**
     * Creates an pattern voter node operation with given voter class.
     */
    public static NodeOperation addPatternVoter(final String voterName, final String voterClass, final String pattern) {
        return addOrGetContentNode(voterName).then(addOrSetProperty(PN_CLASS, voterClass), addOrSetProperty(PN_PATTERN, pattern));
    }

    private NodeOperationFactory() {
    }

    /**
     * Operation to create node path by given type. Adds only missing nodes in the path.
     *
     * @author frank.sommer
     * @since 21.02.2014
     */
    private static class CreatePathNodeOperation extends AbstractNodeOperation {
        private final String _relPath;
        private final String _type;

        CreatePathNodeOperation(final String relPath, final String type) {
            _relPath = relPath;
            _type = type;
        }

        @Override
        protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
            Node result;
            final String cleanRelPath = strip(_relPath, PATH_SEPARATOR);

            if (context.hasNode(cleanRelPath)) {
                result = context.getNode(cleanRelPath);
                if (_type != null && !result.isNodeType(_type)) {
                    throw new RepositoryException("Type of Node '" + cleanRelPath + "' does not match. Expected: " + _type + "   found : " + result.getPrimaryNodeType().getName());
                }
            } else {
                result = context;
                String[] nodeNames = split(cleanRelPath, PATH_SEPARATOR);
                for (String nodeName : nodeNames) {
                    if (result.hasNode(nodeName)) {
                        result = result.getNode(nodeName);
                    } else {
                        String typeForCreation = _type == null ? NodeTypes.Content.NAME : _type;
                        result = result.addNode(nodeName, typeForCreation);
                    }
                }
            }
            return result;
        }
    }
}
