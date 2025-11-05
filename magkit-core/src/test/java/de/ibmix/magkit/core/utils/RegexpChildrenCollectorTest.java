package de.ibmix.magkit.core.utils;

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
 * #L% */

import org.junit.jupiter.api.Test;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.PropertyMockUtils.mockProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link RegexpChildrenCollector} ensuring pattern filtering, type filtering, collection replacement
 * and constructor variants (string vs precompiled pattern) are covered.
 *
 * @author GutHub Copilot, supplemented by wolf.bubenik@ibmix.de
 * @since 2025-10-20
 */
public class RegexpChildrenCollectorTest {

    @Test
    public void collectsMatchingNodesOnlyBeyondRootLevel() throws Exception {
        Collection<Node> result = new ArrayList<>();
        TestableCollector<Node> collector = new TestableCollector<>(result, "child[0-9]", false, 5, Node.class);
        Node root = mockNode("/root");
        Node child1 = mockNode("/root/child1");
        Node child2 = mockNode("/root/child-x");
        Property child2Prop = mockProperty("child2");

        collector.simulateEnter(root, 0);
        collector.simulateEnter(child1, 1);
        collector.simulateEnter(child2, 1);
        collector.simulateEnter(child2Prop, 1);

        assertEquals(1, result.size());
        boolean foundChild1 = false;
        for (Node n : result) {
            if ("child1".equals(n.getName())) {
                foundChild1 = true;
            }
        }
        assertTrue(foundChild1);
        assertEquals("child[0-9]", collector.getChildNamePattern().pattern());
    }

    @Test
    public void collectsMatchingPropertiesOnly() throws Exception {
        Collection<Property> result = new ArrayList<>();
        TestableCollector<Property> collector = new TestableCollector<>(result, Pattern.compile("val[0-9]"), true, 3, Property.class);
        Property p0 = mockProperty("val0");
        Property p2 = mockProperty("val-x");
        Property p1 = mockProperty("val1");
        // should NOT be collected (wrong type)
        Node val2Node = mockNode("/root/val2");

        collector.simulateEnter(p0, 1);
        collector.simulateEnter(p2, 1);
        // different level still > 0
        collector.simulateEnter(p1, 2);
        // ignored due to type filter
        collector.simulateEnter(val2Node, 1);

        assertEquals(2, result.size());
        boolean hasVal0 = false;
        boolean hasVal1 = false;
        boolean hasVal2Node = false;
        for (Property p : result) {
            if ("val0".equals(p.getName())) {
                hasVal0 = true;
            } else if ("val1".equals(p.getName())) {
                hasVal1 = true;
            } else if ("val2".equals(p.getName())) {
                // should never happen
                hasVal2Node = true;
            }
        }
        assertTrue(hasVal0);
        assertTrue(hasVal1);
        assertFalse(hasVal2Node);
    }

    @Test
    public void collectsBothNodesAndPropertiesWhenItemClassRequested() throws Exception {
        Collection<Item> result = new ArrayList<>();
        TestableCollector<Item> collector = new TestableCollector<>(result, "(node|prop)[0-9]", false, 2, Item.class);
        Node node1 = mockNode("/root/node1");
        Property prop1 = mockProperty("prop1");
        Node node2 = mockNode("/root/node-x");
        Property prop2 = mockProperty("prop-x");

        collector.simulateEnter(node1, 1);
        collector.simulateEnter(prop1, 1);
        collector.simulateEnter(node2, 1);
        collector.simulateEnter(prop2, 1);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(i -> {
            try {
                return "node1".equals(i.getName());
            } catch (RepositoryException e) {
                return false;
            }
        }));
        assertTrue(result.stream().anyMatch(i -> {
            try {
                return "prop1".equals(i.getName());
            } catch (RepositoryException e) {
                return false;
            }
        }));
    }

    @Test
    public void replacingCollectedChildrenCollectionWorks() throws Exception {
        Collection<Property> initial = new ArrayList<>();
        TestableCollector<Property> collector = new TestableCollector<>(initial, "p[0-9]", false, 3, Property.class);
        Property p1 = mockProperty("p1");
        collector.simulateEnter(p1, 1);
        assertEquals(1, initial.size());

        Collection<Property> replacement = new ArrayList<>();
        collector.setCollectedChildren(replacement);
        assertEquals(0, replacement.size());
        Property p2 = mockProperty("p2");
        collector.simulateEnter(p2, 1);
        assertEquals(1, initial.size());
        assertEquals(1, replacement.size());
        assertTrue(replacement.stream().anyMatch(p -> {
            try {
                return "p2".equals(p.getName());
            } catch (RepositoryException e) {
                return false;
            }
        }));
        assertSame(replacement, collector.getCollectedChildren());
        assertNotNull(collector.getClassToCollect());
        assertEquals(Property.class, collector.getClassToCollect());
    }

    /**
     * Helper exposing protected entering methods for controlled simulation of traversal levels.
     *
     * @param <X> item subtype under test
     */
    private static final class TestableCollector<X extends Item> extends RegexpChildrenCollector<X> {
        private TestableCollector(Collection<X> collected, String pattern, boolean breadthFirst, int maxLevel, Class<? extends X> clazz) {
            super(collected, pattern, breadthFirst, maxLevel, clazz);
        }
        private TestableCollector(Collection<X> collected, Pattern pattern, boolean breadthFirst, int maxLevel, Class<? extends X> clazz) {
            super(collected, pattern, breadthFirst, maxLevel, clazz);
        }
        public void simulateEnter(Node node, int level) throws RepositoryException {
            entering(node, level);
        }
        public void simulateEnter(Property property, int level) throws RepositoryException {
            entering(property, level);
        }
    }

}
