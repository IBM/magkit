package de.ibmix.magkit.core.node;

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

import org.apache.commons.collections4.IteratorUtils;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * A {@link NullableDelegateNodeWrapper} that lets callers define an artificial parent for a wrapped {@link Node}.
 * <p>Purpose: Build arbitrary transient node hierarchies (in-memory) while keeping hierarchical metadata (path,
 * depth, parent linkage) consistent without modifying the underlying repository.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Synthetic path calculation based on provided parent wrapper.</li>
 *   <li>Consistent depth computation (+1 from defined parent).</li>
 *   <li>Automatic wrapping of child nodes to maintain hierarchy invariants.</li>
 *   <li>Works with real JCR nodes – no repository writes performed.</li>
 * </ul>
 * <p>Usage example:</p>
 * <pre>{@code
 * Node actual = ...; // obtained from a JCR Session
 * Node syntheticParent = ...; // some existing parent or another wrapper
 * DefineParentNodeWrapper wrapped = new DefineParentNodeWrapper(syntheticParent, actual);
 * String path = wrapped.getPath(); // synthetic path below the defined parent
 * }</pre>
 * <p>Null and error handling: The constructor validates that the provided parent is non-null. Standard JCR exceptions
 * from delegate calls are propagated unchanged.</p>
 * <p>Thread-safety: Instances are not thread-safe if the wrapped node changes, but immutable with respect to their own
 * parent reference after construction.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-02-16
 */
public class DefineParentNodeWrapper extends NullableDelegateNodeWrapper {

    private Node _parent;

    /**
     * Create a wrapper with a synthetic parent reference.
     *
     * @param parent the parent node to be reported by hierarchy related methods (must not be null)
     * @param node the actual node being wrapped (may not be null, validated by super)
     */
    public DefineParentNodeWrapper(final Node parent, final Node node) {
        super(node);
        notNull(parent);
        _parent = parent;
    }

    /**
     * Returns a synthetic path composed of the parent path and this node's name.
     *
     * @return synthetic absolute path
     * @throws RepositoryException if parent path resolution fails
     */
    @Override
    public String getPath() throws RepositoryException {
        return _parent.getPath() + '/' + getName();
    }

    /**
     * Returns the defined synthetic parent.
     *
     * @return defined parent node
     * @throws RepositoryException if underlying parent access fails
     */
    @Override
    public Node getParent() throws RepositoryException {
        return _parent;
    }

    /**
     * Returns depth as parent depth + 1.
     *
     * @return synthetic depth value
     * @throws RepositoryException if parent depth retrieval fails
     */
    @Override
    public int getDepth() throws RepositoryException {
        return _parent.getDepth() + 1;
    }

    /**
     * Wrap the addressed child node ensuring hierarchy consistency.
     *
     * @param relPath relative path to the child
     * @return wrapped child with this instance as synthetic parent
     * @throws PathNotFoundException if child cannot be found
     * @throws RepositoryException on other repository access problems
     */
    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        return new DefineParentNodeWrapper(this, super.getNode(relPath));
    }

    /**
     * Returns wrapped iterator of all child nodes.
     *
     * @return iterator with hierarchy aware wrappers
     * @throws RepositoryException if child enumeration fails
     */
    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return toWrappedNodesIterator(getWrappedNode().getNodes());
    }

    /**
     * Returns wrapped iterator filtered by name pattern.
     *
     * @param namePattern JCR name pattern
     * @return iterator with hierarchy aware wrappers
     * @throws RepositoryException if child enumeration fails
     */
    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return toWrappedNodesIterator(getWrappedNode().getNodes(namePattern));
    }

    /**
     * Returns wrapped iterator filtered by glob patterns.
     *
     * @param nameGlobs array of glob patterns
     * @return iterator with hierarchy aware wrappers
     * @throws RepositoryException if child enumeration fails
     */
    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return toWrappedNodesIterator(getWrappedNode().getNodes(nameGlobs));
    }

    private NodeIterator toWrappedNodesIterator(NodeIterator nodes) {
        return new NodeIteratorAdapter(((Collection<Node>) IteratorUtils.toList(nodes))
            .stream()
            .map(n -> isWrapping(n) ? n : new DefineParentNodeWrapper(this, n))
            .collect(Collectors.toList()));
    }
}
