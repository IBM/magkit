package com.aperto.magkit.utils;

import org.apache.log4j.Logger;
import info.magnolia.cms.core.NodeData;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Collection;

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
}
