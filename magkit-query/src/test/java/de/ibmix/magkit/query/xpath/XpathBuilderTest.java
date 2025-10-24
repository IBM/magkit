package de.ibmix.magkit.query.xpath;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.*;

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

    @BeforeEach
    public void init() {
        _xpath = new XpathBuilder();
    }

    @Test
    public void testInitialize() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH, _xpath.build());
    }

    @Test
    public void testPath() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + TEST_PATH, _xpath.path(TEST_PATH).build());
    }

    @Test
    public void testPathWithNumbers() {
        assertEquals("/jcr:root/test/content/_x0030_0", _xpath.path("/test/content/00").build());
    }

    @Test
    public void testPathDouble() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + TEST_PATH + TEST_PATH, _xpath.path(TEST_PATH).path(TEST_PATH).build());
    }

    @Test
    public void testTypeNull() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + NULL_STRING + "," + NULL_STRING + ")", _xpath.type(null, null).build());
    }

    @Test
    public void testEmptyType() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_EMPTY, _xpath.emptyType().build());
    }

    @Test
    public void testEmptyTypeDouble() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_EMPTY + XpathBuilder.SELECTOR_TYPE_EMPTY, _xpath.emptyType().emptyType().build());
    }

    @Test
    public void testType() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + XpathBuilder.SELECTOR_ALL + "," + TEST_TYPE + ")", _xpath.type(XpathBuilder.SELECTOR_ALL, TEST_TYPE).build());
    }

    @Test
    public void testTypeDouble() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + XpathBuilder.SELECTOR_ALL + "," + TEST_TYPE + ")" + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + XpathBuilder.SELECTOR_ALL + "," + TEST_TYPE + ")", _xpath.type(XpathBuilder.SELECTOR_ALL, TEST_TYPE).type(XpathBuilder.SELECTOR_ALL, TEST_TYPE).build());
    }

    @Test
    public void testTypeAll() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + XpathBuilder.SELECTOR_ALL + "," + XpathBuilder.SELECTOR_ALL + ")", _xpath.type(XpathBuilder.SELECTOR_ALL, XpathBuilder.SELECTOR_ALL).build());
    }

    @Test
    public void testTypeWithName() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + TEST_NAME + "," + TEST_TYPE + ")", _xpath.type(TEST_NAME, TEST_TYPE).build());
    }

    @Test
    public void testPropertyNull() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + "[" + NULL_STRING + "]", _xpath.property((String) null).build());
    }

    @Test
    public void testPropertyEmpty() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + "[]", _xpath.property(new ConstraintBuilder()).build());
    }

    @Test
    public void testProperty() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + "[" + TEST_CONSTRAINT + "]", _xpath.property(new ConstraintBuilder().add(TEST_CONSTRAINT)).build());
    }

    @Test
    public void testPropertyDouble() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + "[" + TEST_CONSTRAINT + "]" + "[" + TEST_CONSTRAINT + "]", _xpath.property(new ConstraintBuilder().add(TEST_CONSTRAINT)).property(new ConstraintBuilder().add(TEST_CONSTRAINT)).build());
    }

    @Test
    public void testOrderByNull() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_ORDER_BY + NULL_STRING, _xpath.orderBy(null).build());
    }

    @Test
    public void testOrderByEmpty() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_ORDER_BY, _xpath.orderBy(EMPTY).build());
    }

    @Test
    public void testOrderBy() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_ORDER_BY + TEST_TYPE, _xpath.orderBy(TEST_TYPE).build());
    }

    @Test
    public void testOrderByDouble() {
        assertEquals(XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_ORDER_BY + TEST_TYPE + XpathBuilder.SELECTOR_ORDER_BY + TEST_TYPE, _xpath.orderBy(TEST_TYPE).orderBy(TEST_TYPE).build());
    }

    @Test
    public void testBuild() {
        final String buildQuery = _xpath.path(TEST_PATH).emptyType().property(new ConstraintBuilder().add(TEST_CONSTRAINT)).path(TEST_PATH).type(TEST_NAME, TEST_TYPE).property(new ConstraintBuilder().add(TEST_CONSTRAINT)).orderBy(TEST_TYPE).build();
        final String expectedQuery = XpathBuilder.JCR_ROOT_PATH + TEST_PATH + XpathBuilder.SELECTOR_TYPE_EMPTY + "[" + TEST_CONSTRAINT + "]" + TEST_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + TEST_NAME + "," + TEST_TYPE + ")[" + TEST_CONSTRAINT + "]" + XpathBuilder.SELECTOR_ORDER_BY + TEST_TYPE;

        assertEquals(expectedQuery, buildQuery);
        // trigger a npe
        assertThrows(NullPointerException.class, () -> _xpath.build());
    }
}
