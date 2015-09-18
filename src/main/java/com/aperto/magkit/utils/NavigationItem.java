package com.aperto.magkit.utils;

import org.apache.commons.collections.CollectionUtils;

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
     */
    public NavigationItem(String key, String value) {
        super(key, value);
    }

    /**
     * Constructor.
     */
    public NavigationItem(String key, String value, List<NavigationItem> subItems) {
        super(key, value);
        _subItems = subItems;
    }

    /**
     * True, if the item has sub items.
     */
    public boolean hasSubItems() {
        return !CollectionUtils.isEmpty(_subItems);
    }

    /**
     * Flag, if the item should be rendered as selected Item. Default is 'false'.
     * @return True, if marked as selected.
     */
    public boolean isSelected() {
        return _selected;
    }

    /**
     * Flag, if the item should be rendered as selected Item. Default is 'false'.
     */
    public void setSelected(boolean selected) {
        _selected = selected;
    }

}
