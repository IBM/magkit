package com.aperto.magkit.export;

import java.util.*;

/**
 * Extends the Properties by sorting.
 *
 * @author frank.sommer (19.08.2008)
 */
public class SortedProperties extends Properties {
    // CHECKSTYLE:OFF
    @Override
    public synchronized Enumeration keys() {
        Enumeration keysEnum = super.keys();
        Vector keyList = new Vector();
        while (keysEnum.hasMoreElements()) {
            keyList.add(keysEnum.nextElement());
        }
        Collections.sort(keyList);
        return keyList.elements();
    }
    // CHECKSTYLE:ON
}
