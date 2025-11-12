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

import de.ibmix.magkit.core.utils.NodeUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static de.ibmix.magkit.core.utils.PropertyUtils.getBooleanValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getBooleanValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getCalendarValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getCalendarValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getDoubleValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getDoubleValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getLongValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getLongValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getStringValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getStringValues;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockComponentNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.node.PageNodeStubbingOperation.stubTemplate;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Test AlteringNodeWrapper.
 *
 * @author wolf.bubenik
 * @since 2019-05-10
 */
public class AlteringNodeWrapperTest {

    @AfterEach
    public void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void testAlteringNodeWrapperDefaults() throws RepositoryException {
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper("test", "nt:unstructured");
        assertEquals("test", nodeWrapper.getName());
        assertEquals("nt:unstructured", nodeWrapper.getPrimaryNodeType().getName());
        assertFalse(nodeWrapper.getNodes().hasNext());
        assertFalse(nodeWrapper.getProperties().hasNext());
    }

    @Test
    public void getNodeToWrap() {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertEquals(node, nodeWrapper.getWrappedNode());
    }

    @Test
    public void withStringProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertNull(getStringValue(nodeWrapper.getProperty("names")));
        nodeWrapper.withProperty("names", "value1", "value2");
        assertEquals("value1", getStringValue(nodeWrapper.getProperty("names")));
        assertTrue(nodeWrapper.getProperty("names").isMultiple());
        List<String> stringValues = getStringValues(nodeWrapper, "names");
        assertEquals(2, stringValues.size());
        Iterator<String> iterator = stringValues.iterator();
        assertEquals("value1", iterator.next());
        assertEquals("value2", iterator.next());
    }

    @Test
    public void withBooleanProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertNull(getBooleanValue(nodeWrapper.getProperty("test")));
        nodeWrapper.withProperty("test", true, false);
        assertEquals(true, getBooleanValue(nodeWrapper.getProperty("test")));
        assertTrue(nodeWrapper.getProperty("test").isMultiple());
        List<Boolean> values = getBooleanValues(nodeWrapper.getProperty("test"));
        assertEquals(2, values.size());
        Iterator<Boolean> iterator = values.iterator();
        assertEquals(true, iterator.next());
        assertEquals(false, iterator.next());
    }

    @Test
    public void withLongProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertNull(getLongValue(nodeWrapper.getProperty("test")));
        nodeWrapper.withProperty("test", 3L, 2L);
        assertEquals(3L, getLongValue(nodeWrapper.getProperty("test")));
        assertTrue(nodeWrapper.getProperty("test").isMultiple());
        List<Long> values = getLongValues(nodeWrapper.getProperty("test"));
        assertEquals(2, values.size());
        Iterator<Long> iterator = values.iterator();
        assertEquals(3L, iterator.next());
        assertEquals(2L, iterator.next());
    }

    @Test
    public void withCalendarProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertNull(getCalendarValue(nodeWrapper.getProperty("test")));
        Calendar now = Calendar.getInstance();
        nodeWrapper.withProperty("test", now);
        assertEquals(now, getCalendarValue(nodeWrapper.getProperty("test")));
        assertEquals(1, getCalendarValues(nodeWrapper.getProperty("test")).size());
        assertEquals(now, getCalendarValues(nodeWrapper.getProperty("test")).get(0));
        assertFalse(nodeWrapper.getProperty("test").isMultiple());
    }

    @Test
    public void withDoubleProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertNull(getDoubleValue(nodeWrapper.getProperty("test")));
        nodeWrapper.withProperty("test", 3.2D, 2.3D);
        assertEquals(3.2D, getDoubleValue(nodeWrapper.getProperty("test")));
        assertTrue(nodeWrapper.getProperty("test").isMultiple());
        List<Double> values = getDoubleValues(nodeWrapper.getProperty("test"));
        assertEquals(2, values.size());
        Iterator<Double> iterator = values.iterator();
        assertEquals(3.2D, iterator.next());
        assertEquals(2.3D, iterator.next());
    }

    @Test
    public void withTemplate() throws Exception {
        Node node = mockNode("test", stubTemplate("test-template"));
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertEquals("test-template", NodeUtils.getTemplate(nodeWrapper));
        nodeWrapper.withTemplate("wrapped-template");
        assertEquals("wrapped-template", NodeUtils.getTemplate(nodeWrapper));
    }

    @Test
    public void getProperty() throws Exception {
        Node node = mockNode("test");
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(node);
        assertNull(getStringValue(wrapper.getProperty("p0")));
        stubProperty("p0", "test-value").of(node);
        assertEquals("test-value", getStringValue(wrapper.getProperty("p0")));
        assertFalse(wrapper.getProperty("p0").isMultiple());
        wrapper.withProperty("p0", "wrapped");
        assertEquals("wrapped", getStringValue(wrapper.getProperty("p0")));
        assertFalse(wrapper.getProperty("p0").isMultiple());
    }

    @Test
    public void hasProperty() throws Exception {
        Node node = mockNode("test");
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(node);
        assertFalse(wrapper.hasProperty("test"));
        assertFalse(wrapper.hasProperty("mapped"));
        stubProperty("test", "test-value").of(node);
        assertTrue(wrapper.hasProperty("test"));
        assertFalse(wrapper.hasProperty("mapped"));
    }

    /**
     * Verifies merging and overriding of properties and hiding logic using getProperties().
     */
    @Test
    public void hiddenPropertyAndOverrideMerge() throws Exception {
        Node base = mockNode("base",
            stubProperty("title", "base-title"),
            stubProperty("keep", "keep-value")
        );
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(base)
            .withProperty("title", "wrapped-title")
            .withProperty("added", "new-value")
            .withHiddenProperty("keep");
        assertEquals("wrapped-title", getStringValue(wrapper.getProperty("title")));
        assertNull(wrapper.getProperty("keep"));
        assertFalse(wrapper.hasProperty("keep"));
        assertEquals("new-value", getStringValue(wrapper.getProperty("added")));
        Set<String> names = new HashSet<>();
        PropertyIterator it = wrapper.getProperties();
        while (it.hasNext()) {
            names.add(it.nextProperty().getName());
        }
        assertTrue(names.contains("title"));
        assertTrue(names.contains("added"));
        assertFalse(names.contains("keep"));
    }

    /**
     * Tests merging synthetic child nodes, overriding existing ones and hiding originals.
     */
    @Test
    public void childNodeMergeHideAndOverride() throws Exception {
        Node root = mockNode("root");
        mockNode("root/a");
        mockNode("root/b", stubProperty("bProp", "b-value"));
        Node injected1 = mockNode("aInjected", stubProperty("x", "y"));
        Node injected3 = mockNode("cInjected", stubProperty("cProp", "c-value"));
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(root)
            .withChildNode("a", injected1)
            .withChildNode("c", injected3)
            .withHiddenNode("b");
        assertFalse(wrapper.hasNode("b"));
        assertNull(wrapper.getNode("b"));
        assertTrue(wrapper.hasNode("a"));
        assertEquals("y", getStringValue(wrapper.getNode("a").getProperty("x")));
        assertTrue(wrapper.hasNode("c"));
        assertEquals("c-value", getStringValue(wrapper.getNode("c").getProperty("cProp")));
        Set<String> childNames = new HashSet<>();
        NodeIterator ni = wrapper.getNodes();
        while (ni.hasNext()) {
            childNames.add(ni.nextNode().getName());
        }
        assertTrue(childNames.contains("aInjected"));
        assertTrue(childNames.contains("cInjected"));
        assertFalse(childNames.contains("b"));
    }

    /**
     * Ensures fallback to nearest ancestor page resolves property absent on wrapped component.
     */
    @Test
    public void fallbackToPageResolvesAncestorProperty() throws Exception {
        mockPageNode("content/page", stubProperty("test", "page-value"));
        Node component = mockComponentNode("content/page/component");
        FallbackNodeWrapper wrapper = new AlteringNodeWrapper(component).withFallbackToPage();
        assertEquals("page-value", getStringValue(wrapper.getProperty("test")));
    }

    /**
     * Verifies custom ancestor predicate fallback resolves property from matched ancestor node.
     */
    @Test
    public void fallbackToAncestorCustomPredicate() throws Exception {
        mockNode("root/ancestor", stubProperty("foo", "bar"));
        Node child = mockNode("root/ancestor/child/grandchild");
        FallbackNodeWrapper wrapper = new AlteringNodeWrapper(child)
            .withFallbackToAncestor(n -> {
                try {
                    return n.getPath().endsWith("/ancestor");
                } catch (Exception e) {
                    return false;
                }
            });
        assertEquals("bar", getStringValue(wrapper.getProperty("foo")));
    }

    /**
     * Ensures reference fallback resolves property from target node referenced by link property.
     */
    @Test
    public void fallbackToReferenceResolvesLinkedNode() throws Exception {
        mockPageNode("root/pageRef", stubProperty("refProp", "ref-value"));
        Node source = mockPageNode("root/pageRef/component", stubProperty("link", "/root/pageRef"));
        FallbackNodeWrapper wrapper = new AlteringNodeWrapper(source).withFallbackToReference("website", "link");
        assertEquals("ref-value", getStringValue(wrapper.getProperty("refProp")));
    }

    /**
     * Checks immutable wrapper blocks mutating operations while allowing reads.
     */
    @Test
    public void immutablePreventsUnderlyingMutations() throws Exception {
        Node node = mockNode("immut", stubProperty("p", "v"));
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(node).immutable();
        Node wrapped = wrapper.getWrappedNode();
        assertTrue(wrapped instanceof ImmutableNodeWrapper);
        assertEquals("v", getStringValue(wrapper.getProperty("p")));
        assertThrows(UnsupportedOperationException.class, () -> wrapped.setProperty("x", "y"));
    }

    /**
     * Confirms hidden properties (underlying + stubbed) are inaccessible and not reported via hasProperty.
     */
    @Test
    public void hiddenPropertyOnUnderlyingAndStubbed() throws Exception {
        Node node = mockNode("hideNode", stubProperty("toHide", "orig"));
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(node)
            .withHiddenProperty("toHide")
            .withProperty("other", "val")
            .withHiddenProperty("other");
        assertNull(wrapper.getProperty("toHide"));
        assertFalse(wrapper.hasProperty("toHide"));
        assertNull(wrapper.getProperty("other"));
        assertFalse(wrapper.hasProperty("other"));
    }

    /**
     * Validates name pattern filtering works with overridden and hidden child nodes.
     */
    @Test
    public void overrideChildNodeKeepsNamePatternFiltering() throws Exception {
        Node root = mockNode("root");
        mockNode("root/alpha");
        mockNode("root/beta");
        Node injected = mockNode("alphaInjected", stubProperty("marker", "yes"));
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(root).withChildNode("alpha", injected).withHiddenNode("beta");
        NodeIterator filtered = wrapper.getNodes("alpha");
        List<String> names = new ArrayList<>();
        while (filtered.hasNext()) {
            names.add(filtered.nextNode().getName());
        }
        assertEquals(1, names.size());
        assertEquals("alphaInjected", names.get(0));
        assertEquals("yes", getStringValue(wrapper.getNode("alpha").getProperty("marker")));
    }

    /**
     * Validates glob name filtering with multiple names and hidden nodes excluded from iterator.
     */
    @Test
    public void overrideChildNodeGlobFiltering() throws Exception {
        Node root = mockNode("globRoot");
        mockNode("globRoot/alpha");
        mockNode("globRoot/beta");
        Node injected = mockNode("alphaInjected", stubProperty("marker", "glob"));
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(root)
            .withChildNode("alpha", injected)
            .withHiddenNode("beta")
            .withChildNode("gamma", mockNode("gammaInjected"));
        NodeIterator filtered = wrapper.getNodes(new String[]{"alpha", "beta", "gamma"});
        List<String> names = new ArrayList<>();
        while (filtered.hasNext()) {
            names.add(filtered.nextNode().getName());
        }
        assertFalse(names.contains("beta"));
        assertTrue(names.contains("alphaInjected"));
        assertTrue(names.contains("gammaInjected"));
    }
}
