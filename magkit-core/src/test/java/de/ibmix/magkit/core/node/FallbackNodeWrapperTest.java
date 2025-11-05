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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for {@link FallbackNodeWrapper} covering property and node fallback chains, name fallback mapping,
 * custom conditions and validation error paths.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-20
 */
public class FallbackNodeWrapperTest {

    @AfterEach
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
        assertEquals(primary, wrapper.getWrappedNode());
        assertEquals("v", PropertyUtils.getStringValue(wrapper.getProperty("p")));
    }

    /**
     * Verifies synthetic constructor with fallback nodes resolves properties.
     */
    @Test
    public void syntheticConstructorAndFallbackResolution() throws Exception {
        Node fallback = mockNode("fallback", stubProperty("title", "fallback-title"));
        FallbackNodeWrapper wrapper = new FallbackNodeWrapper("synthetic", "mgnl:content");
        wrapper.withFallbackNodes(fallback);
        assertEquals("fallback-title", PropertyUtils.getStringValue(wrapper.getProperty("title")));
    }

    /**
     * Ensures null property condition raises NPE.
     */
    @Test
    public void withPropertyConditionNullThrows() throws Exception {
        Node primary = mockNode("p1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        assertThrows(NullPointerException.class, () -> wrapper.withPropertyCondition(null));
    }

    /**
     * Ensures null iterator condition raises NPE.
     */
    @Test
    public void withIteratorConditionNullThrows() throws Exception {
        Node primary = mockNode("p1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        assertThrows(NullPointerException.class, () -> wrapper.withIteratorCondition(null));
    }

    /**
     * Ensures null fallback node array raises NPE.
     */
    @Test
    public void withFallbackNodesNullThrows() throws Exception {
        Node primary = mockNode("p1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        assertThrows(NullPointerException.class, () -> wrapper.withFallbackNodes((Node[]) null));
    }

    /**
     * Ensures empty property name or fallback names are validated.
     */
    @Test
    public void withPropertyNameFallbacksNullNameThrows() throws Exception {
        Node primary = mockNode("p1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        assertThrows(NullPointerException.class, () -> wrapper.withPropertyNameFallbacks(null, "alt"));
    }

    /**
     * Ensures empty fallback name array is rejected.
     */
    @Test
    public void withPropertyNameFallbacksEmptyFallbacksThrows() throws Exception {
        Node primary = mockNode("p1");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        assertThrows(IllegalArgumentException.class, () -> wrapper.withPropertyNameFallbacks("title"));
    }

    /**
     * Verifies direct child node resolution from primary.
     */
    @Test
    public void getNodePrimaryPresent() throws Exception {
        Node parent = mockNode("root");
        Node child = mockNode("root/child", stubProperty("x", "y"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(parent);
        assertEquals(child, wrapper.getNode("child"));
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
        assertEquals(fb, wrapper.getNode("child"));
    }

    /**
     * Missing child returns null when no fallback has it.
     */
    @Test
    public void getNodeNoFallbackReturnsNull() throws Exception {
        Node primary = mockNode("primary");
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary);
        assertNull(wrapper.getNode("missing"));
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
        assertTrue(it.hasNext());
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
        assertTrue(names.contains("matchOne"));
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
        assertTrue(names.contains("test1"));
    }

    /**
     * Property resolution uses primary value when present and valid.
     */
    @Test
    public void getPropertyPrimaryNoFallback() throws Exception {
        Node primary = mockNode("primary", stubProperty("title", "main"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withFallbackNodes(mockNode("fb", stubProperty("title", "fb")));
        assertEquals("main", PropertyUtils.getStringValue(wrapper.getProperty("title")));
    }

    /**
     * Property fallback used when primary missing property.
     */
    @Test
    public void getPropertyFallbackNodeUsed() throws Exception {
        Node primary = mockNode("primary");
        Node fb = mockNode("fb", stubProperty("title", "fb-title"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withFallbackNodes(fb);
        assertEquals("fb-title", PropertyUtils.getStringValue(wrapper.getProperty("title")));
    }

    /**
     * Name fallback mapping resolves alternative name on same node.
     */
    @Test
    public void getPropertyNameFallbacksOnSameNode() throws Exception {
        Node primary = mockNode("primary", stubProperty("displayTitle", "DT"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withPropertyNameFallbacks("title", "displayTitle");
        assertEquals("DT", PropertyUtils.getStringValue(wrapper.getProperty("title")));
    }

    /**
     * Skips empty primary value and uses alternative name.
     */
    @Test
    public void getPropertyNameFallbacksSkipEmptyPrimaryValue() throws Exception {
        Node primary = mockNode("primary", stubProperty("title", ""), stubProperty("displayTitle", "NonEmpty"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withPropertyNameFallbacks("title", "displayTitle");
        assertEquals("NonEmpty", PropertyUtils.getStringValue(wrapper.getProperty("title")));
    }

    /**
     * Uses ordered fallback list when earlier names absent.
     */
    @Test
    public void getPropertyNameFallbacksOrderUsed() throws Exception {
        Node primary = mockNode("primary", stubProperty("altTitle", "ALT"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withPropertyNameFallbacks("title", "displayTitle", "altTitle");
        assertEquals("ALT", PropertyUtils.getStringValue(wrapper.getProperty("title")));
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
        assertEquals("long-value", PropertyUtils.getStringValue(wrapper.getProperty("title")));
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
        assertTrue(nit.hasNext());
        assertEquals("a", primaryChild.getName());
        assertEquals("a", nit.nextNode().getName());
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
        assertNull(PropertyUtils.getStringValue(wrapper.getProperty("title")));
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
        assertEquals("FB", PropertyUtils.getStringValue(wrapper.getProperty("title")));
    }

    /**
     * Null entries in fallback node array are filtered out.
     */
    @Test
    public void fallbackNodesNullFiltered() throws Exception {
        Node fb = mockNode("fb", stubProperty("x", "y"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(mockNode("primary"))
            .withFallbackNodes(null, fb, null);
        assertEquals("y", PropertyUtils.getStringValue(wrapper.getProperty("x")));
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
        assertNull(wrapper.getProperty("title"));
    }

    /**
     * Node iterator empty when no primary or fallback node satisfies condition.
     */
    @Test
    public void nodeIteratorEmptyWithoutChildrenAnywhere() throws Exception {
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(mockNode("primary"));
        NodeIterator nit = wrapper.getNodes();
        assertFalse(nit.hasNext());
    }

    /**
     * Mapping configured but none of the names exist -> null.
     */
    @Test
    public void propertyNameFallbacksNoneExistReturnsNull() throws Exception {
        Node primary = mockNode("primary", stubProperty("unrelated", "value"));
        FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary).withPropertyNameFallbacks("title", "displayTitle", "altTitle");
        assertNull(wrapper.getProperty("title"));
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
        assertFalse(nit.hasNext());
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
        assertNull(wrapper.getProperty("title"));
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
        assertTrue(names.contains("alpha"));
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
        assertTrue(names.contains("beta1"));
    }
}
