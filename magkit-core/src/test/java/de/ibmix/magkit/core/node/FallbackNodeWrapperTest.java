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

import de.ibmix.magkit.core.utils.PropertyUtils;
import org.junit.After;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.NodeIterator;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.List;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Tests for {@link FallbackNodeWrapper} covering property and node fallback chains, name fallback mapping,
 * custom conditions and validation error paths.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-20
 */
public class FallbackNodeWrapperTest {

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }

    /**
     * Verifies factory method returns wrapper delegating to primary node.
     */
    @Test
    public void factoryMethodCreatesWrapper() throws Exception {
        Node primary = mockNode("primary", stubProperty("p", "v"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        assertThat(wrapper.getWrappedNode(), is(primary));
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("p")), is("v"));
    }

    /**
     * Verifies synthetic constructor with fallback nodes resolves properties.
     */
    @Test
    public void syntheticConstructorAndFallbackResolution() throws Exception {
        Node fallback = mockNode("fallback", stubProperty("title", "fallback-title"));
        FallbackNodeWrapper wrapper = new FallbackNodeWrapper("synthetic", "mgnl:content");
        wrapper.withFallbackNodes(fallback);
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("title")), is("fallback-title"));
    }

    /**
     * Ensures null property condition raises NPE.
     */
    @Test(expected = NullPointerException.class)
    public void withPropertyConditionNullThrows() throws Exception {
        Node primary = mockNode("p1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        wrapper.withPropertyCondition(null);
    }

    /**
     * Ensures null iterator condition raises NPE.
     */
    @Test(expected = NullPointerException.class)
    public void withIteratorConditionNullThrows() throws Exception {
        Node primary = mockNode("p1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        wrapper.withIteratorCondition(null);
    }

    /**
     * Ensures null fallback node array raises NPE.
     */
    @Test(expected = NullPointerException.class)
    public void withFallbackNodesNullThrows() throws Exception {
        Node primary = mockNode("p1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        wrapper.withFallbackNodes((Node[]) null);
    }

    /**
     * Ensures empty property name or fallback names are validated.
     */
    @Test(expected = NullPointerException.class)
    public void withPropertyNameFallbacksNullNameThrows() throws Exception {
        Node primary = mockNode("p1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        wrapper.withPropertyNameFallbacks(null, "alt");
    }

    /**
     * Ensures empty fallback name array is rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void withPropertyNameFallbacksEmptyFallbacksThrows() throws Exception {
        Node primary = mockNode("p1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        wrapper.withPropertyNameFallbacks("title");
    }

    /**
     * Verifies direct child node resolution from primary.
     */
    @Test
    public void getNodePrimaryPresent() throws Exception {
        Node parent = mockNode("root");
        Node child = mockNode("root/child", stubProperty("x", "y"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(parent);
        assertThat(wrapper.getNode("child"), is(child));
    }

    /**
     * Verifies fallback node used when primary lacks child.
     */
    @Test
    public void getNodeFallbackUsed() throws Exception {
        Node primary = mockNode("primary");
        Node fallbackParent = mockNode("fallback");
        Node fb = mockNode("fallback/child", stubProperty("x", "y"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withFallbackNodes(fallbackParent);
        assertThat(wrapper.getNode("child"), is(fb));
    }

    /**
     * Missing child returns null when no fallback has it.
     */
    @Test
    public void getNodeNoFallbackReturnsNull() throws Exception {
        Node primary = mockNode("primary");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        assertThat(wrapper.getNode("missing"), nullValue());
    }

    /**
     * Iterator falls back when primary has no children.
     */
    @Test
    public void getNodesFallbackWhenPrimaryEmpty() throws Exception {
        Node primary = mockNode("primary");
        Node child1 = mockNode("fb/child1");
        Node child2 = mockNode("fb/child2");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withFallbackNodes(mockNode("fb"), child1, child2);
        NodeIterator it = wrapper.getNodes();
        assertThat(it.hasNext(), is(true));
    }

    /**
     * Pattern based node iterator falls back.
     */
    @Test
    public void getNodesPatternFallback() throws Exception {
        mockNode("fb/matchOne");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(mockNode("primary"))
            .withFallbackNodes(mockNode("fb"));
        NodeIterator it = wrapper.getNodes("match*");
        List<String> names = new ArrayList<>();
        while (it.hasNext()) {
            names.add(it.nextNode().getName());
        }
        assertThat(names.contains("matchOne"), is(true));
    }

    /**
     * Glob patterns trigger fallback.
     */
    @Test
    public void getNodesGlobsFallback() throws Exception {
        mockNode("fb/test1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(mockNode("primary"))
            .withFallbackNodes(mockNode("fb"));
        NodeIterator it = wrapper.getNodes(new String[]{"test*"});
        List<String> names = new ArrayList<>();
        while (it.hasNext()) {
            names.add(it.nextNode().getName());
        }
        assertThat(names.contains("test1"), is(true));
    }

    /**
     * Property resolution uses primary value when present and valid.
     */
    @Test
    public void getPropertyPrimaryNoFallback() throws Exception {
        Node primary = mockNode("primary", stubProperty("title", "main"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withFallbackNodes(mockNode("fb", stubProperty("title", "fb")));
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("title")), is("main"));
    }

    /**
     * Property fallback used when primary missing property.
     */
    @Test
    public void getPropertyFallbackNodeUsed() throws Exception {
        Node primary = mockNode("primary");
        Node fb = mockNode("fb", stubProperty("title", "fb-title"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withFallbackNodes(fb);
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("title")), is("fb-title"));
    }

    /**
     * Name fallback mapping resolves alternative name on same node.
     */
    @Test
    public void getPropertyNameFallbacksOnSameNode() throws Exception {
        Node primary = mockNode("primary", stubProperty("displayTitle", "DT"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withPropertyNameFallbacks("title", "displayTitle");
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("title")), is("DT"));
    }

    /**
     * Skips empty primary value and uses alternative name.
     */
    @Test
    public void getPropertyNameFallbacksSkipEmptyPrimaryValue() throws Exception {
        Node primary = mockNode("primary", stubProperty("title", ""), stubProperty("displayTitle", "NonEmpty"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withPropertyNameFallbacks("title", "displayTitle");
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("title")), is("NonEmpty"));
    }

    /**
     * Uses ordered fallback list when earlier names absent.
     */
    @Test
    public void getPropertyNameFallbacksOrderUsed() throws Exception {
        Node primary = mockNode("primary", stubProperty("altTitle", "ALT"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withPropertyNameFallbacks("title", "displayTitle", "altTitle");
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("title")), is("ALT"));
    }

    /**
     * Custom property condition filters short values causing node fallback.
     */
    @Test
    public void customPropertyConditionSkipsShortValues() throws Exception {
        Node primary = mockNode("primary", stubProperty("title", "abc"));
        Node fb = mockNode("fb", stubProperty("title", "long-value"));
        Predicate<Property> longValuePredicate = p -> {
            String v = PropertyUtils.getStringValue(p);
            return v != null && v.length() > 5;
        };
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary)
            .withFallbackNodes(fb)
            .withPropertyCondition(longValuePredicate);
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("title")), is("long-value"));
    }

    /**
     * Primary node iterator wins when primary has children.
     */
    @Test
    public void nodeIteratorPrimaryWinsWhenConditionPasses() throws Exception {
        Node primaryChild = mockNode("primary/a");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(mockNode("primary"));
        wrapper.withFallbackNodes(mockNode("fb"));
        NodeIterator nit = wrapper.getNodes();
        assertThat(nit.hasNext(), is(true));
        assertThat(primaryChild.getName(), is("a"));
        assertThat(nit.nextNode().getName(), is("a"));
    }

    /**
     * Returns null when all mapped property names resolve to empty values.
     */
    @Test
    public void propertyNameFallbacksAllEmptyReturnsNull() throws Exception {
        Node primary = mockNode("primary",
            stubProperty("title", ""),
            stubProperty("displayTitle", ""),
            stubProperty("altTitle", "")
        );
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary)
            .withPropertyNameFallbacks("title", "displayTitle", "altTitle");
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("title")), nullValue());
    }

    /**
     * Cross node name fallback: primary lacks property but fallback node has alternative name.
     */
    @Test
    public void propertyNameFallbacksCrossNode() throws Exception {
        Node primary = mockNode("primary");
        Node fb = mockNode("fb", stubProperty("displayTitle", "FB"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary)
            .withFallbackNodes(fb)
            .withPropertyNameFallbacks("title", "displayTitle", "altTitle");
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("title")), is("FB"));
    }

    /**
     * Null entries in fallback node array are filtered out.
     */
    @Test
    public void fallbackNodesNullFiltered() throws Exception {
        Node fb = mockNode("fb", stubProperty("x", "y"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(mockNode("primary"))
            .withFallbackNodes(null, fb, null);
        assertThat(PropertyUtils.getStringValue(wrapper.getProperty("x")), is("y"));
    }

    /**
     * Custom condition causing no property in chain to match returns null.
     */
    @Test
    public void customPropertyConditionNoMatchReturnsNull() throws Exception {
        Node primary = mockNode("primary", stubProperty("title", "abc"));
        Node fb = mockNode("fb", stubProperty("title", "xyz"));
        Predicate<Property> never = p -> false;
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withFallbackNodes(fb).withPropertyCondition(never);
        assertThat(wrapper.getProperty("title"), nullValue());
    }

    /**
     * Node iterator empty when no primary or fallback node satisfies condition.
     */
    @Test
    public void nodeIteratorEmptyWithoutChildrenAnywhere() throws Exception {
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(mockNode("primary"));
        NodeIterator nit = wrapper.getNodes();
        assertThat(nit.hasNext(), is(false));
    }

    /**
     * Mapping configured but none of the names exist -> null.
     */
    @Test
    public void propertyNameFallbacksNoneExistReturnsNull() throws Exception {
        Node primary = mockNode("primary", stubProperty("unrelated", "value"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withPropertyNameFallbacks("title", "displayTitle", "altTitle");
        assertThat(wrapper.getProperty("title"), nullValue());
    }

    /**
     * Iterator condition always false returns empty iterator even with fallback candidates.
     */
    @Test
    public void customIteratorConditionAlwaysFalseReturnsEmpty() throws Exception {
        Node primary = mockNode("primary/a");
        Node fb = mockNode("fb/b");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(mockNode("primary"))
            .withFallbackNodes(mockNode("fb"), primary, fb)
            .withIteratorCondition(it -> false);
        NodeIterator nit = wrapper.getNodes();
        assertThat(nit.hasNext(), is(false));
    }

    /**
     * Fallback chain nodes exist but share empty property values -> null.
     */
    @Test
    public void propertyFallbackChainAllEmpty() throws Exception {
        Node primary = mockNode("primary", stubProperty("title", ""));
        Node fb1 = mockNode("fb1", stubProperty("title", ""));
        Node fb2 = mockNode("fb2", stubProperty("title", ""));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withFallbackNodes(fb1, fb2);
        assertThat(wrapper.getProperty("title"), nullValue());
    }

    /**
     * Property iterator fallback triggered by custom iterator condition requiring >=2 properties.
     */
    @Test
    public void propertyIteratorFallbackWithCustomCondition() throws Exception {
        // only jcr:primaryType present
        Node primary = mockNode("primary");
        Node fb = mockNode("fb", stubProperty("alpha", "a"));
        Predicate<Iterator<?>> atLeastTwo = it -> {
            int count = 0;
            while (it.hasNext() && count < 2) {
                it.next();
                count++;
            }
            return count >= 2;
        };
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary)
            .withFallbackNodes(fb)
            .withIteratorCondition(atLeastTwo);
        PropertyIterator pit = wrapper.getProperties();
        List<String> names = new ArrayList<>();
        while (pit.hasNext()) {
            names.add(pit.nextProperty().getName());
        }
        assertThat(names.contains("alpha"), is(true));
    }

    /**
     * Pattern filtered property iterator fallback under custom condition.
     */
    @Test
    public void propertyIteratorPatternFallbackWithCustomCondition() throws Exception {
        Node primary = mockNode("primary");
        Node fb = mockNode("fb", stubProperty("beta1", "b"));
        Predicate<Iterator<?>> atLeastTwo = it -> {
            int count = 0;
            while (it.hasNext() && count < 2) {
                it.next();
                count++;
            }
            return count >= 2;
        };
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary)
            .withFallbackNodes(fb)
            .withIteratorCondition(atLeastTwo);
        PropertyIterator pit = wrapper.getProperties("beta*");
        List<String> names = new ArrayList<>();
        while (pit.hasNext()) {
            names.add(pit.nextProperty().getName());
        }
        assertThat(names.contains("beta1"), is(true));
    }
}
