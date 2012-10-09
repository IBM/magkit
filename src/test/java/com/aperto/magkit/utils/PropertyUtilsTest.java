package com.aperto.magkit.utils;

import junit.framework.TestCase;

import static com.aperto.magkit.utils.PropertyUtils.retrieveMultiSelectProperties;

/**
 * Test for {@link PropertyUtils}.
 *
 * @author frank.sommer
 * @since 09.10.12
 */
public class PropertyUtilsTest extends TestCase {

    public void testNullValue() throws Exception {
        retrieveMultiSelectProperties(null);
    }

    public void testRetrieveMultiSelectProperties() throws Exception {
        //TODO: implement this
    }

    public void testRetrieveMultiSelectValues() throws Exception {
        //TODO: implement this
    }

    public void testRetrieveOrderedMultiSelectValues() throws Exception {
        //TODO: implement this
    }
}
