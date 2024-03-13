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

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.DelegateNodeWrapper;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A DelegateNodeWrapper that allows building an arbitrary node hierarchy and keeping the hierarchy data in sync (path, depth, parent).
 * Introduced for the content-hub.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-02-16
 */
public class DefineParentNodeWrapper extends DelegateNodeWrapper {

    private Node _parent;

    public DefineParentNodeWrapper(final Node parent, final Node node) {
        super(node);
        _parent = parent;
    }

    @Override
    public String getPath() throws RepositoryException {
        return _parent.getPath() + '/' + getName();
    }

    @Override
    public Node getParent() throws RepositoryException {
        return _parent;
    }

    @Override
    public int getDepth() throws RepositoryException {
        return _parent.getDepth() + 1;
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        return new DefineParentNodeWrapper(this, super.getNode(relPath));
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return toWrappedNodesIterator(getWrappedNode().getNodes());
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return toWrappedNodesIterator(getWrappedNode().getNodes(namePattern));
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return toWrappedNodesIterator(getWrappedNode().getNodes(nameGlobs));
    }

    private NodeIterator toWrappedNodesIterator(NodeIterator nodes) {
        return new NodeIteratorAdapter((Collection) IteratorUtils.toList(nodes)
            .stream()
            .map(n -> isWrapping((Node) n) ? n : new DefineParentNodeWrapper(this, (Node) n))
            .collect(Collectors.toList()));
    }
}
