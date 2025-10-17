package de.ibmix.magkit.core.utils;

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

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * NavigationItem extends {@link Item} to represent a hierarchical navigation structure.
 * It holds an optional list of child navigation items (sub items) allowing tree-like navigation models
 * and a selected flag indicating if the item should be rendered as the currently active element.
 * <p>
 * Key features:
 * <ul>
 *   <li>Optional list of sub items (can be null or empty).</li>
 *   <li>Convenience method {@link #hasSubItems()} to check for existing children.</li>
 *   <li>Selection state managed via {@link #isSelected()} and {@link #setSelected(boolean)}.</li>
 * </ul>
 * <p>
 * Null handling: The list of sub items may be null to denote absence; callers should use {@link #hasSubItems()} before iterating.
 * No side effects: All mutators only change internal fields without external interactions.
 * Thread-safety: This class is NOT thread-safe. If shared across threads, external synchronization is required.
 * <p>
 * Typical usage example:
 * <pre>
 * NavigationItem root = new NavigationItem("home", "/home");
 * NavigationItem child = new NavigationItem("about", "/about");
 * root.setSubItems(List.of(child));
 * root.setSelected(true);
 * </pre>
 *
 * @author frank.sommer (09.05.2008)
 * @since 2008-05-09
 */
public class NavigationItem extends Item {
    private List<NavigationItem> _subItems = null;
    private boolean _selected = false;

    /**
     * Returns the list of child navigation items or null if none were set.
     * Use {@link #hasSubItems()} to safely determine if children exist.
     *
     * @return list of sub items or null if absent
     */
    public List<NavigationItem> getSubItems() {
        return _subItems;
    }

    /**
     * Sets the list of child navigation items. Accepts null to clear existing children.
     *
     * @param subItems list of sub items or null
     */
    public void setSubItems(List<NavigationItem> subItems) {
        _subItems = subItems;
    }

    /**
     * Default constructor creating an empty navigation item with no sub items and not selected.
     */
    public NavigationItem() {
        super();
    }

    /**
     * Creates a navigation item with key and value (e.g., identifier and URL) and no sub items.
     *
     * @param key   item key (identifier)
     * @param value item value (e.g., URL or label)
     */
    public NavigationItem(String key, String value) {
        super(key, value);
    }

    /**
     * Creates a navigation item with key, value and an initial list of sub items.
     *
     * @param key      item key (identifier)
     * @param value    item value (e.g., URL or label)
     * @param subItems initial sub items list (may be null)
     */
    public NavigationItem(String key, String value, List<NavigationItem> subItems) {
        super(key, value);
        _subItems = subItems;
    }

    /**
     * Indicates whether this item has at least one sub item.
     *
     * @return true if sub items list is non-null and not empty
     */
    public boolean hasSubItems() {
        return !CollectionUtils.isEmpty(_subItems);
    }

    /**
     * Returns whether this item is marked as selected.
     *
     * @return true if selected
     */
    public boolean isSelected() {
        return _selected;
    }

    /**
     * Sets the selection state of this item.
     *
     * @param selected true to mark as selected, false otherwise
     */
    public void setSelected(boolean selected) {
        _selected = selected;
    }

}
