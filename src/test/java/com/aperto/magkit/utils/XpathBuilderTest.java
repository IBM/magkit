package com.aperto.magkit.utils;

import static com.aperto.magkit.utils.XpathBuilder.JCR_ROOT_PATH;
import static com.aperto.magkit.utils.XpathBuilder.SELECTOR_ALL;
import static com.aperto.magkit.utils.XpathBuilder.SELECTOR_ORDER_BY;
import static com.aperto.magkit.utils.XpathBuilder.SELECTOR_TYPE_ELEMENT;
import static com.aperto.magkit.utils.XpathBuilder.SELECTOR_TYPE_EMPTY;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author philipp.guettler
 * @since 16.09.13
 */
public class XpathBuilderTest {

    private XpathBuilder _xpath;

    private static final String NULL_STRING = "null";
    private static final String TEST_PATH = "test/path/to/element";
    private static final String TEST_TYPE = "test:type";
    private static final String TEST_NAME = "testName";
    private static final String TEST_CONSTRAINT = "testConstraint";

    @Before
    public void init() {
        _xpath = new XpathBuilder();
    }

    @Test
    public void testInitialize() {
        assertEquals(_xpath.build(), JCR_ROOT_PATH);
    }

    @Test
    public void testPath() {
        assertEquals(_xpath.path(TEST_PATH).build(), JCR_ROOT_PATH + TEST_PATH);
    }

    @Test
    public void testPathWithNumbers() {
        assertEquals(_xpath.path("/test/content/00").build(), "/jcr:root/test/content/_x0030_0");
    }

    @Test
    public void testPathDouble() {
        assertEquals(_xpath.path(TEST_PATH).path(TEST_PATH).build(), JCR_ROOT_PATH + TEST_PATH + TEST_PATH);
    }

    @Test
    public void testTypeNull() {
        assertEquals(_xpath.type(null, null).build(), JCR_ROOT_PATH + SELECTOR_TYPE_ELEMENT + "(" + NULL_STRING + "," + NULL_STRING + ")");
    }

    @Test
    public void testEmptyType() {
        assertEquals(_xpath.emptyType().build(), JCR_ROOT_PATH + SELECTOR_TYPE_EMPTY);
    }

    @Test
    public void testEmptyTypeDouble() {
        assertEquals(_xpath.emptyType().emptyType().build(), JCR_ROOT_PATH + SELECTOR_TYPE_EMPTY + SELECTOR_TYPE_EMPTY);
    }

    @Test
    public void testType() {
        assertEquals(_xpath.type(SELECTOR_ALL, TEST_TYPE).build(), JCR_ROOT_PATH + SELECTOR_TYPE_ELEMENT + "(" + SELECTOR_ALL + "," + TEST_TYPE + ")");
    }

    @Test
    public void testTypeDouble() {
        assertEquals(_xpath.type(SELECTOR_ALL, TEST_TYPE).type(SELECTOR_ALL, TEST_TYPE).build(), JCR_ROOT_PATH + SELECTOR_TYPE_ELEMENT + "(" + SELECTOR_ALL + "," + TEST_TYPE + ")" + SELECTOR_TYPE_ELEMENT + "(" + SELECTOR_ALL + "," + TEST_TYPE + ")");
    }

    @Test
    public void testTypeAll() {
        assertEquals(_xpath.type(SELECTOR_ALL, SELECTOR_ALL).build(), JCR_ROOT_PATH + SELECTOR_TYPE_ELEMENT + "(" + SELECTOR_ALL + "," + SELECTOR_ALL + ")");
    }

    @Test
    public void testTypeWithName() {
        assertEquals(_xpath.type(TEST_NAME, TEST_TYPE).build(), JCR_ROOT_PATH + SELECTOR_TYPE_ELEMENT + "(" + TEST_NAME + "," + TEST_TYPE + ")");
    }

    @Test
    public void testPropertyNull() {
        String nullString = null;
        assertEquals(_xpath.property(nullString).build(), JCR_ROOT_PATH + "[" + NULL_STRING + "]");
    }

    @Test
    public void testPropertyEmpty() {
        assertEquals(_xpath.property(new ConstraintBuilder()).build(), JCR_ROOT_PATH + "[]");
    }

    @Test
    public void testProperty() {
        assertEquals(_xpath.property(new ConstraintBuilder().add(TEST_CONSTRAINT)).build(), JCR_ROOT_PATH + "[" + TEST_CONSTRAINT + "]");
    }

    @Test
    public void testPropertyDouble() {
        assertEquals(_xpath.property(new ConstraintBuilder().add(TEST_CONSTRAINT)).property(new ConstraintBuilder().add(TEST_CONSTRAINT)).build(), JCR_ROOT_PATH + "[" + TEST_CONSTRAINT + "]" + "[" + TEST_CONSTRAINT + "]");
    }

    @Test
    public void testOrderByNull() {
        assertEquals(_xpath.orderBy(null).build(), JCR_ROOT_PATH + SELECTOR_ORDER_BY + NULL_STRING);
    }

    @Test
    public void testOrderByEmpty() {
        assertEquals(_xpath.orderBy(EMPTY).build(), JCR_ROOT_PATH + SELECTOR_ORDER_BY);
    }

    @Test
    public void testOrderBy() {
        assertEquals(_xpath.orderBy(TEST_TYPE).build(), JCR_ROOT_PATH + SELECTOR_ORDER_BY + TEST_TYPE);
    }

    @Test
    public void testOrderByDouble() {
        assertEquals(_xpath.orderBy(TEST_TYPE).orderBy(TEST_TYPE).build(), JCR_ROOT_PATH + SELECTOR_ORDER_BY + TEST_TYPE + SELECTOR_ORDER_BY + TEST_TYPE);
    }

    @Test(expected = NullPointerException.class)
    public void testBuild() {
        final String buildQuery = _xpath.path(TEST_PATH).emptyType().property(new ConstraintBuilder().add(TEST_CONSTRAINT)).path(TEST_PATH).type(TEST_NAME, TEST_TYPE).property(new ConstraintBuilder().add(TEST_CONSTRAINT)).orderBy(TEST_TYPE).build();
        final String expectedQuery = JCR_ROOT_PATH + TEST_PATH + SELECTOR_TYPE_EMPTY + "[" + TEST_CONSTRAINT + "]" + TEST_PATH + SELECTOR_TYPE_ELEMENT + "(" + TEST_NAME + "," + TEST_TYPE + ")[" + TEST_CONSTRAINT + "]" + SELECTOR_ORDER_BY + TEST_TYPE;

        assertEquals(buildQuery, expectedQuery);
        // trigger a npe
        _xpath.build();
    }
}
