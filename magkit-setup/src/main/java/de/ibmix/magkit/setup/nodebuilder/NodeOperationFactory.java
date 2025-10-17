package de.ibmix.magkit.setup.nodebuilder;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import de.ibmix.magkit.setup.delta.StandardTasks;
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

import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.strip;

/**
 * Factory/utility extension of Magnolia's {@link Ops} adding idempotent node and property operations and
 * ordering helpers for JCR content setup tasks.
 * <p>Key features:
 * <ul>
 *     <li>Idempotent creation via addOrGet* methods to avoid duplicate nodes on repeated module updates.</li>
 *     <li>Convenience operations for ordering siblings (orderBefore/orderAfter) wrapping JCR / Magnolia utilities.</li>
 *     <li>Simplified property mutation ignoring previous value (addOrSetProperty).</li>
 *     <li>Bulk removal helpers (removeIfExists, removeAllChilds) to clean node subtrees.</li>
 *     <li>Pattern voter node creation for access / configuration logic.</li>
 * </ul>
 * Usage preconditions: All operations expect a valid JCR {@link Node} context supplied by the NodeBuilder execution
 * environment. Provided relative paths must not be null or empty; node type validation occurs when an existing node
 * is found with a mismatching type (throws {@link RepositoryException}).
 * <p>Side effects: Node and property modifications are performed directly on the provided session and are not
 * automatically saved; callers rely on Magnolia's installation task lifecycle to persist changes. Removal operations
 * delete items irrevocably in the current transient state until save/rollback.</p>
 * <p>Null and error handling: Methods avoid returning null. Type mismatches cause a {@link RepositoryException} in
 * {@code CreatePathNodeOperation}. Unknown property types are converted using {@link PropertyUtil#createValue(Object, javax.jcr.ValueFactory)}.</p>
 * <p>Thread-safety: All methods are stateless and static. Thread-safe regarding internal state; JCR session / node
 * concurrency must be handled externally.</p>
 * <p>Usage example:
 * <pre>{@code
 * NodeOperation op = NodeOperationFactory.addOrGetContentNode("myModule/config")
 *     .then(NodeOperationFactory.addOrSetProperty("enabled", true));
 * }</pre>
 * </p>
 *
 * @author wolf.bubenik
 * @since 2010-09-16
 */
public abstract class NodeOperationFactory extends Ops {
    public static final String PATH_SEPARATOR = "/";

    /**
     * Creates or returns a child node with the given name if it already exists.
     * Idempotent convenience wrapper delegating to {@link #addOrGetNode(String, String)} with no explicit type.
     *
     * @param name the node name (single segment)
     * @return operation that yields the existing or newly created node
     */
    public static NodeOperation addOrGetNode(final String name) {
        return addOrGetNode(name, null);
    }

    /**
     * Creates or returns a child content node (type {@link info.magnolia.jcr.util.NodeTypes.ContentNode#NAME}).
     * Accepts a relative path; intermediate nodes are created as content nodes as needed.
     *
     * @param relPath node name or relative path
     * @return operation that yields the existing or newly created node
     */
    public static NodeOperation addOrGetContentNode(final String relPath) {
        return addOrGetNode(relPath, NodeTypes.ContentNode.NAME);
    }

    /**
     * Creates missing path segments and returns the terminal node. If an existing terminal node has a different
     * primary type than specified, a {@link RepositoryException} is raised.
     *
     * @param relPath relative path ("segment/segment"), trimmed of leading/trailing slashes
     * @param type optional expected node type; if null Magnolia's {@link NodeTypes.Content#NAME} is used for creation
     * @return node operation producing the final node
     */
    public static NodeOperation addOrGetNode(final String relPath, final String type) {
        return new CreatePathNodeOperation(relPath, type);
    }

    /**
     * Orders a sibling so that the node named {@code nodeName} appears directly before {@code orderBeforeNodeName}.
     *
     * @param nodeName name of node to move
     * @param orderBeforeNodeName name of sibling that will follow the moved node
     * @return ordering operation
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
     * Orders a sibling so that the node named {@code nodeName} appears directly after {@code orderAfterNodeName}.
     *
     * @param nodeName name of node to move
     * @param orderAfterNodeName name of sibling that will precede the moved node
     * @return ordering operation
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
     * Sets or creates a property with the provided value regardless of any existing value.
     *
     * @param name property name
     * @param newValue value to set (converted via Magnolia PropertyUtil)
     * @return operation performing mutation
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
     * Removes a node or property if it exists, silently ignoring absent items.
     *
     * @param name child node or property name
     * @return operation performing conditional removal
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
     * Removes all direct child nodes of the current context node.
     *
     * @return operation performing bulk removal
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
     * Adds a URI pattern voter configuration node with a pattern.
     *
     * @param voterName node name for voter configuration
     * @param pattern matching pattern string
     * @return operation building voter node
     */
    public static NodeOperation addUriPatternVoter(final String voterName, final String pattern) {
        return addPatternVoter(voterName, URIPatternVoter.class.getName(), pattern);
    }

    /**
     * Adds a generic pattern voter configuration node.
     *
     * @param voterName node name
     * @param voterClass fully qualified voter implementation class name
     * @param pattern matching pattern string
     * @return operation building voter node
     */
    public static NodeOperation addPatternVoter(final String voterName, final String voterClass, final String pattern) {
        return addOrGetContentNode(voterName).then(addOrSetProperty(StandardTasks.PN_CLASS, voterClass), addOrSetProperty(StandardTasks.PN_PATTERN, pattern));
    }

    private NodeOperationFactory() {
    }

    /**
     * Internal operation creating a path of nodes using either the provided type or Magnolia's content type.
     * Performs type validation on existing terminal node.
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
