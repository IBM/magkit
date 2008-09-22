package com.aperto.magkit.utils;

import org.apache.log4j.Logger;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataStringComparator;
import java.util.*;

/**
 * Util class for handle magnolia content.
 *
 * @author frank.sommer (15.05.2008)
 */
public final class ContentUtils {
    private static final Logger LOGGER = Logger.getLogger(ContentUtils.class);

    /**
     * Orders the given collection of NodeDatas by name.
     * @param collection of NodeDatas
     * @return ordered collection
     */
    public static Collection orderNodeDataCollection(Collection collection) {
        Iterator it = collection.iterator();
        NodeData[] nodes = new NodeData[collection.size()];
        while (it.hasNext()) {
            NodeData nd = (NodeData) it.next();
            nodes[Integer.valueOf(nd.getName())] = nd;
        }
        return Arrays.asList(nodes);
    }

    private ContentUtils() {
    }

    public static Collection orderNodeDataCollectionByValue(Collection collection) {
        List<NodeData> nodeDataList = (List<NodeData>) collection;
        Collections.sort(nodeDataList, new NodeDataComparator());
        return nodeDataList;
    }
}
