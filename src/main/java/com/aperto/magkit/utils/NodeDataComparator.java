package com.aperto.magkit.utils;

import info.magnolia.cms.core.NodeData;
import java.util.Comparator;

/**
 * Comparator for a list of NodeDatas.
 *
 * @author frank.sommer (22.09.2008)
 */
public class NodeDataComparator implements Comparator<NodeData> {
    public int compare(NodeData o1, NodeData o2) {
        return o1.getString().compareToIgnoreCase(o2.getString());
    }
}
