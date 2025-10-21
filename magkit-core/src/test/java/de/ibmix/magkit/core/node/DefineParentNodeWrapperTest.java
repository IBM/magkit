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
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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

    @Before
    public void setUp() {
        SessionMockUtils.cleanSession();
    }

    @Test(expected = NullPointerException.class)
    public void constructorRequiresNonNullParent() throws RepositoryException {
        // build hierarchy for actual
        Node root = mockNode("root");
        Node actual = mockNode("root/actual");
        new DefineParentNodeWrapper(null, actual);
    }

    @Test
    public void pathAndParentAndDepthAreSynthetic() throws RepositoryException {
        Node syntheticParent = mockNode("root");
        Node actual = mockNode("root/actual");
        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(syntheticParent, actual);

        assertThat(wrapper.getParent(), is(syntheticParent));
        assertThat(wrapper.getPath(), is("/root/actual"));
        assertThat(wrapper.getDepth(), is(syntheticParent.getDepth() + 1));
    }

    @Test
    public void getNodeWrapsChildWithThisAsParent() throws RepositoryException {
        Node syntheticParent = mockNode("syntheticParent");
        Node actual = mockNode("root/actual");
        Node child = mockNode("root/actual/child");

        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(syntheticParent, actual);
        Node childWrapper = wrapper.getNode("child");
        assertThat(childWrapper instanceof DefineParentNodeWrapper, is(true));
        assertThat(childWrapper.getParent(), is(wrapper));
        assertThat(childWrapper.getPath(), is(wrapper.getPath() + "/child"));
        assertThat(childWrapper.getDepth(), is(wrapper.getDepth() + 1));
    }

    @Test(expected = PathNotFoundException.class)
    public void getNodePropagatesPathNotFound() throws RepositoryException {
        Node syntheticParent = mockNode("root");
        Node actual = mockNode("root/actual");
        when(actual.getNode("missing")).thenThrow(new PathNotFoundException("missing"));
        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(syntheticParent, actual);
        wrapper.getNode("missing");
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
        assertThat(children.size(), is(2));
        DefineParentNodeWrapper first = (DefineParentNodeWrapper) children.get(0);
        DefineParentNodeWrapper second = (DefineParentNodeWrapper) children.get(1);
        assertThat(first.getParent(), is(wrapper));
        assertThat(second.getParent(), is(wrapper));
        assertThat(second.getPath(), is("/syntheticParent/nodeA/child2"));
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
        assertThat(children.size(), is(1));
        assertThat(children.get(0) instanceof DefineParentNodeWrapper, is(true));
        assertThat(children.get(0).getParent(), is(wrapper));
    }

    @Test
    public void getNodesByGlobs() throws RepositoryException {
        Node syntheticParent = mockNode("syntheticParent");
        Node actual = mockNode("root/actual");

        Node child1 = mockNode("root/actual/child1");
        Node child2 = mockNode("root/actual/child2");

        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(syntheticParent, actual);
        NodeIterator wrappedIterator = wrapper.getNodes(new String[]{"child1", "child2"});
        List<Node> children = toList(wrappedIterator);
        assertThat(children.size(), is(2));
        assertThat(children.get(0) instanceof DefineParentNodeWrapper, is(true));
        assertThat(children.get(1) instanceof DefineParentNodeWrapper, is(true));
        assertThat(children.get(0).getParent(), is(wrapper));
        assertThat(children.get(1).getParent(), is(wrapper));
    }

    @Test
    public void nestedWrappingBuildsConsistentHierarchy() throws RepositoryException {
        Node root = mockNode("newRoot");
        Node actual = mockNode("root/actual");
        Node child = mockNode("root/actual/child");
        Node leaf = mockNode("root/actual/child/leaf");

        DefineParentNodeWrapper wrapper = new DefineParentNodeWrapper(root, actual);
        DefineParentNodeWrapper childWrapper = (DefineParentNodeWrapper) wrapper.getNode("child");
        DefineParentNodeWrapper leafWrapper = (DefineParentNodeWrapper) childWrapper.getNode("leaf");

        assertThat(childWrapper.getParent(), is(wrapper));
        assertThat(childWrapper.getPath(), is("/newRoot/actual/child"));
        assertThat(childWrapper.getDepth(), is(wrapper.getDepth() + 1));

        assertThat(leafWrapper.getParent(), is(childWrapper));
        assertThat(leafWrapper.getPath(), is("/newRoot/actual/child/leaf"));
        assertThat(leafWrapper.getDepth(), is(childWrapper.getDepth() + 1));
    }

    private List<Node> toList(NodeIterator iterator) {
        List<Node> nodes = new ArrayList<>();
        while (iterator.hasNext()) {
            nodes.add(iterator.nextNode());
        }
        return nodes;
    }
}
