package de.ibmix.magkit.core.node;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2025 IBM iX
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

import de.ibmix.magkit.test.jcr.SessionMockUtils;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefineParentNodeWrapper} covering synthetic hierarchy behaviour (path, depth, parent),
 * child wrapping logic and null safety of constructor.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-20
 */
public class DefineParentNodeWrapperTest {

    @BeforeEach
    public void setUp() {
        SessionMockUtils.cleanSession();
    }

    @Test
    public void constructorRequiresNonNullParent() throws RepositoryException {
        // build hierarchy for actual
        Node root = mockNode("root");
        Node actual = mockNode("root/actual");
        assertThrows(NullPointerException.class, () -> new DefineParentNodeWrapper(null, actual));
    }

    @Test
    public void pathAndParentAndDepthAreSynthetic() throws RepositoryException {
        Node syntheticParent = mockNode("root");
        Node actual = mockNode("root/actual");
        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(syntheticParent, actual);

        assertEquals(syntheticParent, wrapper.getParent());
        assertEquals("/root/actual", wrapper.getPath());
        assertEquals(syntheticParent.getDepth() + 1, wrapper.getDepth());
    }

    @Test
    public void getNodeWrapsChildWithThisAsParent() throws RepositoryException {
        Node syntheticParent = mockNode("syntheticParent");
        Node actual = mockNode("root/actual");
        mockNode("root/actual/child");
        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(syntheticParent, actual);
        Node childWrapper = wrapper.getNode("child");
        assertTrue(childWrapper instanceof DefineParentNodeWrapper);
        assertEquals(wrapper, childWrapper.getParent());
        assertEquals(wrapper.getPath() + "/child", childWrapper.getPath());
        assertEquals(wrapper.getDepth() + 1, childWrapper.getDepth());
    }

    @Test
    public void getNodePropagatesPathNotFound() throws RepositoryException {
        Node syntheticParent = mockNode("root");
        Node actual = mockNode("root/actual");
        when(actual.getNode("missing")).thenThrow(new PathNotFoundException("missing"));
        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(syntheticParent, actual);
        assertThrows(PathNotFoundException.class, () -> wrapper.getNode("missing"));
    }

    @Test
    public void getNodesWrapsChildrenAndKeepsAlreadyWrappedOnes() throws RepositoryException {
        Node syntheticParent = mockNode("syntheticParent");
        Node actual = mockNode("root/nodeA");
        mockNode("root/nodeA/child1");
        mockNode("root/nodeA/child2");
        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(syntheticParent, actual);
        NodeIterator wrappedIterator = wrapper.getNodes();
        List<Node> children = toList(wrappedIterator);
        assertEquals(2, children.size());
        DefineParentNodeWrapper first = (DefineParentNodeWrapper) children.get(0);
        DefineParentNodeWrapper second = (DefineParentNodeWrapper) children.get(1);
        assertEquals(wrapper, first.getParent());
        assertEquals(wrapper, second.getParent());
        assertEquals("/syntheticParent/nodeA/child2", second.getPath());
    }

    @Test
    public void getNodesByNamePattern() throws RepositoryException {
        Node syntheticParent = mockNode("syntheticRoot");
        Node actual = mockNode("root/node");
        Node child = mockNode("root/node/child-a");
        doReturn(new NodeIteratorAdapter(Collections.singletonList(child))).when(actual).getNodes();
        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(syntheticParent, actual);
        NodeIterator wrappedIterator = wrapper.getNodes("child*");
        List<Node> children = toList(wrappedIterator);
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof DefineParentNodeWrapper);
        assertEquals(wrapper, children.get(0).getParent());
    }

    @Test
    public void getNodesByGlobs() throws RepositoryException {
        Node syntheticParent = mockNode("syntheticParent");
        Node actual = mockNode("root/actual");
        mockNode("root/actual/child1");
        mockNode("root/actual/child2");
        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(syntheticParent, actual);
        NodeIterator wrappedIterator = wrapper.getNodes(new String[]{"child1", "child2"});
        List<Node> children = toList(wrappedIterator);
        assertEquals(2, children.size());
        assertTrue(children.get(0) instanceof DefineParentNodeWrapper);
        assertTrue(children.get(1) instanceof DefineParentNodeWrapper);
        assertEquals(wrapper, children.get(0).getParent());
        assertEquals(wrapper, children.get(1).getParent());
    }

    @Test
    public void nestedWrappingBuildsConsistentHierarchy() throws RepositoryException {
        Node root = mockNode("newRoot");
        Node actual = mockNode("root/actual");
        mockNode("root/actual/child");
        mockNode("root/actual/child/leaf");
        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(root, actual);
        DefineParentNodeWrapper childWrapper = (DefineParentNodeWrapper) wrapper.getNode("child");
        DefineParentNodeWrapper leafWrapper = (DefineParentNodeWrapper) childWrapper.getNode("leaf");
        assertEquals(wrapper, childWrapper.getParent());
        assertEquals("/newRoot/actual/child", childWrapper.getPath());
        assertEquals(wrapper.getDepth() + 1, childWrapper.getDepth());
        assertEquals(childWrapper, leafWrapper.getParent());
        assertEquals("/newRoot/actual/child/leaf", leafWrapper.getPath());
        assertEquals(childWrapper.getDepth() + 1, leafWrapper.getDepth());
    }

    private List<Node> toList(NodeIterator iterator) {
        List<Node> nodes = new ArrayList<>();
        while (iterator.hasNext()) {
            nodes.add(iterator.nextNode());
        }
        return nodes;
    }
}
