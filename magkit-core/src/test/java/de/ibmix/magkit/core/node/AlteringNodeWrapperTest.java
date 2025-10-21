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
import org.junit.After;
import org.junit.Test;

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
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeStubbingOperation.stubTemplate;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

/**
 * Test AlteringNodeWrapper.
 *
 * @author wolf.bubenik
 * @since 2019-05-10
 */
public class AlteringNodeWrapperTest {

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void testAlteringNodeWrapperDefaults() throws RepositoryException {
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper("test", "nt:unstructured");
        assertThat(nodeWrapper.getName(), is("test"));
        assertThat(nodeWrapper.getPrimaryNodeType().getName(), is("nt:unstructured"));
        assertFalse(nodeWrapper.getNodes().hasNext());
        assertFalse(nodeWrapper.getProperties().hasNext());
    }

    @Test
    public void getNodeToWrap() {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(nodeWrapper.getWrappedNode(), is(node));
    }

    @Test
    public void withStringProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(getStringValue(nodeWrapper.getProperty("names")), nullValue());

        nodeWrapper.withProperty("names", "value1", "value2");
        assertThat(getStringValue(nodeWrapper.getProperty("names")), is("value1"));
        assertThat(nodeWrapper.getProperty("names").isMultiple(), is(true));

        List<String> stringValues = getStringValues(nodeWrapper, "names");
        assertThat(stringValues.size(), is(2));
        Iterator<String> iterator = stringValues.iterator();
        assertThat(iterator.next(), is("value1"));
        assertThat(iterator.next(), is("value2"));
    }

    @Test
    public void withBooleanProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(getBooleanValue(nodeWrapper.getProperty("test")), nullValue());

        nodeWrapper.withProperty("test", true, false);
        assertThat(getBooleanValue(nodeWrapper.getProperty("test")), is(true));
        assertThat(nodeWrapper.getProperty("test").isMultiple(), is(true));

        List<Boolean> values = getBooleanValues(nodeWrapper.getProperty("test"));
        assertThat(values.size(), is(2));
        Iterator<Boolean> iterator = values.iterator();
        assertThat(iterator.next(), is(true));
        assertThat(iterator.next(), is(false));
    }

    @Test
    public void withLongProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(getLongValue(nodeWrapper.getProperty("test")), nullValue());

        nodeWrapper.withProperty("test", 3L, 2L);
        assertThat(getLongValue(nodeWrapper.getProperty("test")), is(3L));
        assertThat(nodeWrapper.getProperty("test").isMultiple(), is(true));

        List<Long> values = getLongValues(nodeWrapper.getProperty("test"));
        assertThat(values.size(), is(2));
        Iterator<Long> iterator = values.iterator();
        assertThat(iterator.next(), is(3L));
        assertThat(iterator.next(), is(2L));
    }

    @Test
    public void withCalendarProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(getCalendarValue(nodeWrapper.getProperty("test")), nullValue());

        Calendar now = Calendar.getInstance();
        nodeWrapper.withProperty("test", now);
        assertThat(getCalendarValue(nodeWrapper.getProperty("test")), is(now));
        assertThat(getCalendarValues(nodeWrapper.getProperty("test")).size(), is(1));
        assertThat(getCalendarValues(nodeWrapper.getProperty("test")).get(0), is(now));
        assertThat(nodeWrapper.getProperty("test").isMultiple(), is(false));
    }

    @Test
    public void withDoubleProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(getDoubleValue(nodeWrapper.getProperty("test")), nullValue());

        nodeWrapper.withProperty("test", 3.2D, 2.3D);
        assertThat(getDoubleValue(nodeWrapper.getProperty("test")), is(3.2D));
        assertThat(nodeWrapper.getProperty("test").isMultiple(), is(true));

        List<Double> values = getDoubleValues(nodeWrapper.getProperty("test"));
        assertThat(values.size(), is(2));
        Iterator<Double> iterator = values.iterator();
        assertThat(iterator.next(), is(3.2D));
        assertThat(iterator.next(), is(2.3D));
    }

    @Test
    public void withTemplate() throws Exception {
        Node node = mockNode("test", stubTemplate("test-template"));
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(NodeUtils.getTemplate(nodeWrapper), is("test-template"));

        nodeWrapper.withTemplate("wrapped-template");
        assertThat(NodeUtils.getTemplate(nodeWrapper), is("wrapped-template"));
    }

    @Test
    public void getProperty() throws Exception {
        Node node = mockNode("test");
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(node);
        assertThat(getStringValue(wrapper.getProperty("p0")), nullValue());

        stubProperty("p0", "test-value").of(node);
        assertThat(getStringValue(wrapper.getProperty("p0")), is("test-value"));
        assertThat(wrapper.getProperty("p0").isMultiple(), is(false));

        wrapper.withProperty("p0", "wrapped");
        assertThat(getStringValue(wrapper.getProperty("p0")), is("wrapped"));
        assertThat(wrapper.getProperty("p0").isMultiple(), is(false));
    }

    @Test
    public void hasProperty() throws Exception {
        Node node = mockNode("test");
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(node);
        assertThat(wrapper.hasProperty("test"), is(false));
        assertThat(wrapper.hasProperty("mapped"), is(false));

        stubProperty("test", "test-value").of(node);
        assertThat(wrapper.hasProperty("test"), is(true));
        assertThat(wrapper.hasProperty("mapped"), is(false));
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

        // title overridden
        assertThat(getStringValue(wrapper.getProperty("title")), is("wrapped-title"));
        // hidden property not accessible
        assertThat(wrapper.getProperty("keep"), nullValue());
        assertThat(wrapper.hasProperty("keep"), is(false));
        // added property present
        assertThat(getStringValue(wrapper.getProperty("added")), is("new-value"));

        // merged iterator contains overridden and added, but not hidden
        Set<String> names = new HashSet<>();
        PropertyIterator it = wrapper.getProperties();
        while (it.hasNext()) {
            names.add(it.nextProperty().getName());
        }
        assertThat(names.contains("title"), is(true));
        assertThat(names.contains("added"), is(true));
        assertThat(names.contains("keep"), is(false));
    }

    /**
     * Tests merging synthetic child nodes, overriding existing ones and hiding originals.
     */
    @Test
    public void childNodeMergeHideAndOverride() throws Exception {
        Node root = mockNode("root");
        mockNode("root/a");
        mockNode("root/b", stubProperty("bProp", "b-value"));
        // Synthetic replacement for a with distinguishing property
        Node injected1 = mockNode("aInjected", stubProperty("x", "y"));
        Node injected3 = mockNode("cInjected", stubProperty("cProp", "c-value"));

        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(root)
            .withChildNode("a", injected1)
            .withChildNode("c", injected3)
            .withHiddenNode("b");

        // Hidden b
        assertThat(wrapper.hasNode("b"), is(false));
        assertThat(wrapper.getNode("b"), nullValue());
        // Overridden a now exposes property x
        assertThat(wrapper.hasNode("a"), is(true));
        assertThat(getStringValue(wrapper.getNode("a").getProperty("x")), is("y"));
        // Added c
        assertThat(wrapper.hasNode("c"), is(true));
        assertThat(getStringValue(wrapper.getNode("c").getProperty("cProp")), is("c-value"));

        Set<String> childNames = new HashSet<>();
        NodeIterator ni = wrapper.getNodes();
        while (ni.hasNext()) {
            childNames.add(ni.nextNode().getName());
        }
        assertThat(childNames.contains("aInjected"), is(true));
        assertThat(childNames.contains("cInjected"), is(true));
        assertThat(childNames.contains("b"), is(false));
    }

    /**
     * Ensures fallback to nearest ancestor page resolves property absent on wrapped component.
     */
    @Test
    public void fallbackToPageResolvesAncestorProperty() throws Exception {
        mockPageNode("content/page", stubProperty("test", "page-value"));
        Node component = mockComponentNode("content/page/component");
        FallbackNodeWrapper wrapper = new AlteringNodeWrapper(component).withFallbackToPage();
        assertThat(getStringValue(wrapper.getProperty("test")), is("page-value"));
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
        assertThat(getStringValue(wrapper.getProperty("foo")), is("bar"));
    }

    /**
     * Ensures reference fallback resolves property from target node referenced by link property.
     */
    @Test
    public void fallbackToReferenceResolvesLinkedNode() throws Exception {
        mockPageNode("root/pageRef", stubProperty("refProp", "ref-value"));
        Node source = mockPageNode("root/pageRef/component", stubProperty("link", "/root/pageRef"));
        FallbackNodeWrapper wrapper = new AlteringNodeWrapper(source).withFallbackToReference("website", "link");
        assertThat(getStringValue(wrapper.getProperty("refProp")), is("ref-value"));
    }

    /**
     * Checks immutable wrapper blocks mutating operations while allowing reads.
     */
    @Test
    public void immutablePreventsUnderlyingMutations() throws Exception {
        Node node = mockNode("immut", stubProperty("p", "v"));
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(node).immutable();
        assertThat(wrapper.getWrappedNode() instanceof ImmutableNodeWrapper, is(true));
        assertThat(getStringValue(wrapper.getProperty("p")), is("v"));
        boolean unsupported = false;
        try {
            wrapper.getWrappedNode().setProperty("x", "y");
        } catch (UnsupportedOperationException e) {
            unsupported = true;
        }
        assertThat(unsupported, is(true));
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
        assertThat(wrapper.getProperty("toHide"), nullValue());
        assertThat(wrapper.hasProperty("toHide"), is(false));
        assertThat(wrapper.getProperty("other"), nullValue());
        assertThat(wrapper.hasProperty("other"), is(false));
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
        while (filtered.hasNext()) { names.add(filtered.nextNode().getName()); }
        assertThat(names.size(), is(1));
        assertThat(names.get(0), is("alphaInjected"));
        assertThat(getStringValue(wrapper.getNode("alpha").getProperty("marker")), is("yes"));
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
        while (filtered.hasNext()) { names.add(filtered.nextNode().getName()); }
        // beta muss versteckt sein
        assertThat(names.contains("beta"), is(false));
        // alpha überschrieben und vorhanden
        assertThat(names.contains("alphaInjected"), is(true));
        // gamma hinzugefügt
        assertThat(names.contains("gammaInjected"), is(true));
    }
}
