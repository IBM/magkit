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
 * Extends Item with a list of sub items.
 *
 * @author frank.sommer (09.05.2008)
 */
public class NavigationItem extends Item {
    private List<NavigationItem> _subItems = null;
    private boolean _selected = false;

    /**
     * Accessor for the list of subitems. Default is NULL.
     *
     * @return A java.util.List&lt;NavigationItem&gt; or NULL if not set.
     */
    public List<NavigationItem> getSubItems() {
        return _subItems;
    }

    public void setSubItems(List<NavigationItem> subItems) {
        _subItems = subItems;
    }

    /**
     * Constructor.
     */
    public NavigationItem() {
        super();
    }

    /**
     * Constructor.
     *
     * @param key   key
     * @param value value
     */
    public NavigationItem(String key, String value) {
        super(key, value);
    }

    /**
     * Constructor.
     *
     * @param key      key
     * @param value    value
     * @param subItems sub items
     */
    public NavigationItem(String key, String value, List<NavigationItem> subItems) {
        super(key, value);
        _subItems = subItems;
    }

    /**
     * True, if the item has sub items.
     *
     * @return has sub items
     */
    public boolean hasSubItems() {
        return !CollectionUtils.isEmpty(_subItems);
    }

    /**
     * Flag, if the item should be rendered as selected Item. Default is 'false'.
     *
     * @return true, if marked as selected.
     */
    public boolean isSelected() {
        return _selected;
    }

    /**
     * Flag, if the item should be rendered as selected Item. Default is 'false'.
     *
     * @param selected selected
     */
    public void setSelected(boolean selected) {
        _selected = selected;
    }

}
