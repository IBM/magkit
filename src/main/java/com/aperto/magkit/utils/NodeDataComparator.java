package com.aperto.magkit.utils;

import info.magnolia.cms.core.NodeData;

import java.util.Comparator;

/**
 * Comparator for a list of NodeDatas.
 * It is possible to sort by name or value.
 * Value is default.
 *
 * @deprecated since 4.5, use TODO
 * @author frank.sommer (22.09.2008)
 */
@Deprecated
public class NodeDataComparator implements Comparator<NodeData> {
    private boolean _compareByValue = true;

    public boolean isCompareByValue() {
        return _compareByValue;
    }

    public void setCompareByValue(boolean compareByValue) {
        _compareByValue = compareByValue;
    }

    public int compare(NodeData o1, NodeData o2) {
        int compareRes;
        if (_compareByValue) {
            compareRes = o1.getString().compareToIgnoreCase(o2.getString());
        } else {
            compareRes = o1.getName().compareToIgnoreCase(o2.getName());
        }
        return compareRes;
    }
}