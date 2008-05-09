package com.aperto.magkit.utils;

import org.apache.commons.collections.CollectionUtils;
import java.util.List;

/**
 * Extends Item with a a list of sub items.
 *
 * @author frank.sommer (09.05.2008)
 */
public class NavigationItem extends Item {
    private List<Item> _subItems = null;

    public List<Item> getSubItems() {
        return _subItems;
    }

    public void setSubItems(List<Item> subItems) {
        _subItems = subItems;
    }

    /**
     * Constructor.
     */
    public NavigationItem(String key, String value) {
        super(key, value);
    }

    /**
     * Constructor.
     */
    public NavigationItem(String key, String value, List<Item> subItems) {
        super(key, value);
        _subItems = subItems;
    }

    /**
     * True, if the item has sub items.
     */
    public boolean hasSubItems() {
        return !CollectionUtils.isEmpty(_subItems);
    }
}
