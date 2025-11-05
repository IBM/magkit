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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link NavigationItem} ensuring full coverage of constructors, mutators and conditional logic:
 * <ul>
 *     <li>Default constructor initializes with no sub items and not selected.</li>
 *     <li>Key/value constructor delegates correctly and leaves sub items null.</li>
 *     <li>Full constructor stores provided sub item list reference.</li>
 *     <li>{@link NavigationItem#hasSubItems()} returns false for null and empty list, true for non-empty list.</li>
 *     <li>Selection flag defaults to false and can be toggled via {@link NavigationItem#setSelected(boolean)}.</li>
 *     <li>Sub items can be replaced and cleared (set to null).</li>
 * </ul>
 * Edge cases verified: empty list, null list, list cleared after being set, toggling selection twice.
 *
 * @author GitHub Copilot, assisted by wolf.bubenik@ibmix.de
 * @since 2025-10-20
 */
public class NavigationItemTest {

    @Test
    public void testDefaultConstructor() {
        NavigationItem item = new NavigationItem();
        assertNull(item.getSubItems());
        assertFalse(item.hasSubItems());
        assertFalse(item.isSelected());
    }

    @Test
    public void testKeyValueConstructor() {
        NavigationItem item = new NavigationItem("10#home", "/home");
        // inherited logic from Item
        assertEquals("home", item.getKey());
        assertEquals(10, item.getPosition());
        assertNull(item.getSubItems());
        assertFalse(item.hasSubItems());
        assertFalse(item.isSelected());
    }

    @Test
    public void testFullConstructorWithSubItems() {
        NavigationItem child = new NavigationItem("child", "/c");
        List<NavigationItem> children = Collections.singletonList(child);
        NavigationItem parent = new NavigationItem("root", "/r", children);
        assertSame(children, parent.getSubItems());
        assertTrue(parent.hasSubItems());
    }

    @Test
    public void testHasSubItemsVariants() {
        NavigationItem item = new NavigationItem("k", "v");
        assertFalse(item.hasSubItems());

        item.setSubItems(Collections.emptyList());
        assertNotNull(item.getSubItems());
        assertFalse(item.hasSubItems());

        List<NavigationItem> list = new ArrayList<>();
        list.add(new NavigationItem("a", "/a"));
        item.setSubItems(list);
        assertTrue(item.hasSubItems());
    }

    @Test
    public void testSelectionFlag() {
        NavigationItem item = new NavigationItem("k", "/v");
        assertFalse(item.isSelected());
        item.setSelected(true);
        assertTrue(item.isSelected());
        item.setSelected(false);
        assertFalse(item.isSelected());
    }

    @Test
    public void testSetAndClearSubItems() {
        NavigationItem item = new NavigationItem("k", "/v");
        List<NavigationItem> first = new ArrayList<>();
        first.add(new NavigationItem("a", "/a"));
        item.setSubItems(first);
        assertSame(first, item.getSubItems());
        assertTrue(item.hasSubItems());

        List<NavigationItem> second = new ArrayList<>();
        second.add(new NavigationItem("b", "/b"));
        second.add(new NavigationItem("c", "/c"));
        item.setSubItems(second);
        assertSame(second, item.getSubItems());
        assertEquals(2, item.getSubItems().size());

        item.setSubItems(null);
        assertNull(item.getSubItems());
        assertFalse(item.hasSubItems());
    }
}
