package com.aperto.magkit.export;

import static java.util.Collections.sort;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * Extends the Properties by sorting.
 *
 * @author frank.sommer (19.08.2008)
 */
public class SortedProperties extends Properties {

    @Override
    public Enumeration keys() {
        Enumeration keysEnum = super.keys();
        List keyList = new Vector();
        while (keysEnum.hasMoreElements()) {
            keyList.add(keysEnum.nextElement());
        }
        sort(keyList);
        return ((Vector) keyList).elements();
    }
}