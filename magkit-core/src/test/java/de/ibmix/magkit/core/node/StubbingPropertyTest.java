package de.ibmix.magkit.core.node;

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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils;
import de.ibmix.magkit.test.jcr.NodeMockUtils;
import de.ibmix.magkit.test.jcr.ValueMockUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Testing StubbingProperty.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-01-04
 */
public class StubbingPropertyTest {

    private Node _parent;

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _parent = MagnoliaNodeMockUtils.mockPageNode("root/page");
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void testHierarchy() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "property", "value");
        assertThat(p.getName(), is("property"));
        assertThat(p.getPath(), is("/root/page/property"));
        assertThat(p.getParent(), is(_parent));
        assertThat(p.getDepth(), is(3));
        assertThat(p.getAncestor(-1), nullValue());
        assertThat(p.getAncestor(0).getPath(), is("/"));
        assertThat(p.getAncestor(1).getPath(), is("/root"));
        assertThat(p.getAncestor(2).getPath(), is("/root/page"));
        assertThat(p.getAncestor(3).getPath(), is("/root/page/property"));
        assertThat(p.getAncestor(4), nullValue());
        assertThat(p.getSession(), is(_parent.getSession()));
        // some other static properties:
        assertThat(p.isNew(), is(false));
        assertThat(p.isNode(), is(false));
        assertThat(p.isModified(), is(false));
        assertThat(p.getDefinition(), nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setValueString() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue("test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValue() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(ValueMockUtils.mockValue(0L));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValues() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(new Value[]{ValueMockUtils.mockValue(0L)});
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValueStrings() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(new String[]{"test"});
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValueInputStream() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(Mockito.mock(InputStream.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValueBinary() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(Mockito.mock(Binary.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValueLong() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(123L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValueDouble() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(123.4D);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValueDecimal() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(BigDecimal.ONE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValueCalendar() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(Calendar.getInstance());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValueBoolean() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(true);
    }

    @Test
    public void getValue() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", "value");
        assertThat(p.getValue(), notNullValue());
        assertThat(p.isMultiple(), is(false));

        p = new StubbingProperty(_parent, "test", "value", "other");
        assertThat(p.getValue(), notNullValue());
        assertThat(p.isMultiple(), is(true));
        assertThat(p.getValue().getString(), is("value"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValue10() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").setValue(NodeMockUtils.mockNode("test"));
    }

    @Test
    public void getValues() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", "value");
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(1));
        assertThat(p.isMultiple(), is(false));

        p = new StubbingProperty(_parent, "test", "value", "other");
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(2));
        assertThat(p.isMultiple(), is(true));
        assertThat(p.getValue().getString(), is("value"));
    }

    @Test
    public void getString() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", "value");
        assertThat(p.getString(), is("value"));
        assertThat(p.getType(), is(PropertyType.STRING));
        assertThat(p.getLength(), is(5L));
    }

    @Test
    public void getStream() throws RepositoryException {
        Binary binary = ValueMockUtils.mockBinary("content");
        Property p = new StubbingProperty(_parent, "test", binary);
        assertThat(p.getStream(), is(binary.getStream()));
        assertThat(p.getType(), is(PropertyType.BINARY));
        assertThat(p.getLength(), is(7L));
    }

    @Test
    public void getBinary() throws RepositoryException {
        Binary binary = ValueMockUtils.mockBinary("content äöü");
        Property p = new StubbingProperty(_parent, "test", binary);
        assertThat(p.getBinary(), is(binary));
        assertThat(p.getType(), is(PropertyType.BINARY));
        assertThat(p.getLength(), is(14L));
    }

    @Test
    public void getLong() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", 123L);
        assertThat(p.getLong(), is(123L));
        assertThat(p.getType(), is(PropertyType.LONG));
        assertThat(p.getLength(), is(3L));
    }

    @Test
    public void getDouble() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", 0.123D);
        assertThat(p.getDouble(), is(0.123D));
        assertThat(p.getType(), is(PropertyType.DOUBLE));
        assertThat(p.getLength(), is(5L));
    }

    @Test
    public void getDecimal() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", BigDecimal.ZERO);
        assertThat(p.getDecimal(), is(BigDecimal.ZERO));
        assertThat(p.getType(), is(PropertyType.DECIMAL));
        assertThat(p.getLength(), is(1L));
    }

    @Test
    public void getDate() throws RepositoryException {
        Calendar date = Calendar.getInstance();
        Property p = new StubbingProperty(_parent, "test", date);
        assertThat(p.getDate(), is(date));
        assertThat(p.getType(), is(PropertyType.DATE));
    }

    @Test
    public void getBoolean() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", true);
        assertThat(p.getBoolean(), is(true));
        assertThat(p.getType(), is(PropertyType.BOOLEAN));
        assertThat(p.getLength(), is(4L));
    }

    @Test
    public void getNode() throws RepositoryException {
        Node n = NodeMockUtils.mockNode("test");
        StubbingProperty p = new StubbingProperty(_parent, "test", n, n);
        assertThat(p.getString(), is(n.getIdentifier()));
        assertThat(p.getType(), is(PropertyType.REFERENCE));
        assertThat(p.getNode(), is(n));
        assertThat(p.getNodes().length, is(2));
    }

    @Test(expected = NotImplementedException.class)
    public void getLengths() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").getLengths();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void accept() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").accept(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void save() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").save();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void refresh() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").refresh(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() throws RepositoryException {
        new StubbingProperty(_parent, "test", "value").remove();
    }
}