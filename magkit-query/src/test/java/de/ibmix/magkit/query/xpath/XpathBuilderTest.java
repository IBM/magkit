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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;

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
        Assert.assertEquals(_xpath.build(), XpathBuilder.JCR_ROOT_PATH);
    }

    @Test
    public void testPath() {
        Assert.assertEquals(_xpath.path(TEST_PATH).build(), XpathBuilder.JCR_ROOT_PATH + TEST_PATH);
    }

    @Test
    public void testPathWithNumbers() {
        Assert.assertEquals(_xpath.path("/test/content/00").build(), "/jcr:root/test/content/_x0030_0");
    }

    @Test
    public void testPathDouble() {
        Assert.assertEquals(_xpath.path(TEST_PATH).path(TEST_PATH).build(), XpathBuilder.JCR_ROOT_PATH + TEST_PATH + TEST_PATH);
    }

    @Test
    public void testTypeNull() {
        Assert.assertEquals(_xpath.type(null, null).build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + NULL_STRING + "," + NULL_STRING + ")");
    }

    @Test
    public void testEmptyType() {
        Assert.assertEquals(_xpath.emptyType().build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_EMPTY);
    }

    @Test
    public void testEmptyTypeDouble() {
        Assert.assertEquals(_xpath.emptyType().emptyType().build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_EMPTY + XpathBuilder.SELECTOR_TYPE_EMPTY);
    }

    @Test
    public void testType() {
        Assert.assertEquals(_xpath.type(XpathBuilder.SELECTOR_ALL, TEST_TYPE).build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + XpathBuilder.SELECTOR_ALL + "," + TEST_TYPE + ")");
    }

    @Test
    public void testTypeDouble() {
        Assert.assertEquals(_xpath.type(XpathBuilder.SELECTOR_ALL, TEST_TYPE).type(XpathBuilder.SELECTOR_ALL, TEST_TYPE).build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + XpathBuilder.SELECTOR_ALL + "," + TEST_TYPE + ")" + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + XpathBuilder.SELECTOR_ALL + "," + TEST_TYPE + ")");
    }

    @Test
    public void testTypeAll() {
        Assert.assertEquals(_xpath.type(XpathBuilder.SELECTOR_ALL, XpathBuilder.SELECTOR_ALL).build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + XpathBuilder.SELECTOR_ALL + "," + XpathBuilder.SELECTOR_ALL + ")");
    }

    @Test
    public void testTypeWithName() {
        Assert.assertEquals(_xpath.type(TEST_NAME, TEST_TYPE).build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + TEST_NAME + "," + TEST_TYPE + ")");
    }

    @Test
    public void testPropertyNull() {
        Assert.assertEquals(_xpath.property((String) null).build(), XpathBuilder.JCR_ROOT_PATH + "[" + NULL_STRING + "]");
    }

    @Test
    public void testPropertyEmpty() {
        Assert.assertEquals(_xpath.property(new ConstraintBuilder()).build(), XpathBuilder.JCR_ROOT_PATH + "[]");
    }

    @Test
    public void testProperty() {
        Assert.assertEquals(_xpath.property(new ConstraintBuilder().add(TEST_CONSTRAINT)).build(), XpathBuilder.JCR_ROOT_PATH + "[" + TEST_CONSTRAINT + "]");
    }

    @Test
    public void testPropertyDouble() {
        Assert.assertEquals(_xpath.property(new ConstraintBuilder().add(TEST_CONSTRAINT)).property(new ConstraintBuilder().add(TEST_CONSTRAINT)).build(), XpathBuilder.JCR_ROOT_PATH + "[" + TEST_CONSTRAINT + "]" + "[" + TEST_CONSTRAINT + "]");
    }

    @Test
    public void testOrderByNull() {
        Assert.assertEquals(_xpath.orderBy(null).build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_ORDER_BY + NULL_STRING);
    }

    @Test
    public void testOrderByEmpty() {
        Assert.assertEquals(_xpath.orderBy(EMPTY).build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_ORDER_BY);
    }

    @Test
    public void testOrderBy() {
        Assert.assertEquals(_xpath.orderBy(TEST_TYPE).build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_ORDER_BY + TEST_TYPE);
    }

    @Test
    public void testOrderByDouble() {
        Assert.assertEquals(_xpath.orderBy(TEST_TYPE).orderBy(TEST_TYPE).build(), XpathBuilder.JCR_ROOT_PATH + XpathBuilder.SELECTOR_ORDER_BY + TEST_TYPE + XpathBuilder.SELECTOR_ORDER_BY + TEST_TYPE);
    }

    @Test(expected = NullPointerException.class)
    public void testBuild() {
        final String buildQuery = _xpath.path(TEST_PATH).emptyType().property(new ConstraintBuilder().add(TEST_CONSTRAINT)).path(TEST_PATH).type(TEST_NAME, TEST_TYPE).property(new ConstraintBuilder().add(TEST_CONSTRAINT)).orderBy(TEST_TYPE).build();
        final String expectedQuery = XpathBuilder.JCR_ROOT_PATH + TEST_PATH + XpathBuilder.SELECTOR_TYPE_EMPTY + "[" + TEST_CONSTRAINT + "]" + TEST_PATH + XpathBuilder.SELECTOR_TYPE_ELEMENT + "(" + TEST_NAME + "," + TEST_TYPE + ")[" + TEST_CONSTRAINT + "]" + XpathBuilder.SELECTOR_ORDER_BY + TEST_TYPE;

        assertEquals(buildQuery, expectedQuery);
        // trigger a npe
        _xpath.build();
    }
}
