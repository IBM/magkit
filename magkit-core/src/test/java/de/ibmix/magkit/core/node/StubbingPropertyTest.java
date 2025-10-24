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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing StubbingProperty.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-01-04
 */
public class StubbingPropertyTest {

    private Node _parent;

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _parent = MagnoliaNodeMockUtils.mockPageNode("root/page");
    }

    @AfterEach
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void testHierarchy() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "property", "value");
        assertEquals("property", p.getName());
        assertEquals("/root/page/property", p.getPath());
        assertEquals(_parent, p.getParent());
        assertEquals(3, p.getDepth());
        assertNull(p.getAncestor(-1));
        assertEquals("/", p.getAncestor(0).getPath());
        assertEquals("/root", p.getAncestor(1).getPath());
        assertEquals("/root/page", p.getAncestor(2).getPath());
        assertEquals("/root/page/property", p.getAncestor(3).getPath());
        assertNull(p.getAncestor(4));
        assertEquals(_parent.getSession(), p.getSession());
        assertFalse(p.isNew());
        assertFalse(p.isNode());
        assertFalse(p.isModified());
        assertNull(p.getDefinition());
    }

    @Test
    public void setValueStringUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue("test"));
    }

    @Test
    public void testSetValueUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(ValueMockUtils.mockValue(0L)));
    }

    @Test
    public void testSetValuesUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(new Value[]{ValueMockUtils.mockValue(0L)}));
    }

    @Test
    public void testSetValueStringsUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(new String[]{"test"}));
    }

    @Test
    public void testSetValueInputStreamUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(Mockito.mock(InputStream.class)));
    }

    @Test
    public void testSetValueBinaryUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(Mockito.mock(Binary.class)));
    }

    @Test
    public void testSetValueLongUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(123L));
    }

    @Test
    public void testSetValueDoubleUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(123.4D));
    }

    @Test
    public void testSetValueDecimalUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(BigDecimal.ONE));
    }

    @Test
    public void testSetValueCalendarUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(Calendar.getInstance()));
    }

    @Test
    public void testSetValueBooleanUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(true));
    }

    @Test
    public void getValue() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", "value");
        assertNotNull(p.getValue());
        assertFalse(p.isMultiple());

        p = new StubbingProperty(_parent, "test", "value", "other");
        assertNotNull(p.getValue());
        assertTrue(p.isMultiple());
        assertEquals("value", p.getValue().getString());
    }

    @Test
    public void testSetValueNodeUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").setValue(NodeMockUtils.mockNode("test")));
    }

    @Test
    public void getValues() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", "value");
        assertNotNull(p.getValues());
        assertEquals(1, p.getValues().length);
        assertFalse(p.isMultiple());

        p = new StubbingProperty(_parent, "test", "value", "other");
        assertNotNull(p.getValues());
        assertEquals(2, p.getValues().length);
        assertTrue(p.isMultiple());
        assertEquals("value", p.getValue().getString());
    }

    @Test
    public void getString() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", "value");
        assertEquals("value", p.getString());
        assertEquals(PropertyType.STRING, p.getType());
        assertEquals(5L, p.getLength());
    }

    @Test
    public void getStream() throws RepositoryException {
        Binary binary = ValueMockUtils.mockBinary("content");
        Property p = new StubbingProperty(_parent, "test", binary);
        assertEquals(binary.getStream(), p.getStream());
        assertEquals(PropertyType.BINARY, p.getType());
        assertEquals(7L, p.getLength());
    }

    @Test
    public void getBinary() throws RepositoryException {
        Binary binary = ValueMockUtils.mockBinary("content äöü");
        Property p = new StubbingProperty(_parent, "test", binary);
        assertEquals(binary, p.getBinary());
        assertEquals(PropertyType.BINARY, p.getType());
        assertEquals(14L, p.getLength());
    }

    @Test
    public void getLong() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", 123L);
        assertEquals(123L, p.getLong());
        assertEquals(PropertyType.LONG, p.getType());
        assertEquals(3L, p.getLength());
    }

    @Test
    public void getDouble() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", 0.123D);
        assertEquals(0.123D, p.getDouble());
        assertEquals(PropertyType.DOUBLE, p.getType());
        assertEquals(5L, p.getLength());
    }

    @Test
    public void getDecimal() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, p.getDecimal());
        assertEquals(PropertyType.DECIMAL, p.getType());
        assertEquals(1L, p.getLength());
    }

    @Test
    public void getDate() throws RepositoryException {
        Calendar date = Calendar.getInstance();
        Property p = new StubbingProperty(_parent, "test", date);
        assertEquals(date, p.getDate());
        assertEquals(PropertyType.DATE, p.getType());
    }

    @Test
    public void getBoolean() throws RepositoryException {
        Property p = new StubbingProperty(_parent, "test", true);
        assertTrue(p.getBoolean());
        assertEquals(PropertyType.BOOLEAN, p.getType());
        assertEquals(4L, p.getLength());
    }

    @Test
    public void getNode() throws RepositoryException {
        Node n = NodeMockUtils.mockNode("test");
        StubbingProperty p = new StubbingProperty(_parent, "test", n, n);
        assertEquals(n.getIdentifier(), p.getString());
        assertEquals(PropertyType.REFERENCE, p.getType());
        assertEquals(n, p.getNode());
        assertEquals(2, p.getNodes().length);
    }

    @Test
    public void getLengthsNotImplemented() throws RepositoryException {
        assertThrows(NotImplementedException.class, () -> new StubbingProperty(_parent, "test", "value").getLengths());
    }

    @Test
    public void acceptUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").accept(null));
    }

    @Test
    public void saveUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").save());
    }

    @Test
    public void refreshUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").refresh(true));
    }

    @Test
    public void removeUnsupported() throws RepositoryException {
        assertThrows(UnsupportedOperationException.class, () -> new StubbingProperty(_parent, "test", "value").remove());
    }

    @Test
    public void isSameTrue() throws RepositoryException {
        StubbingProperty p1 = new StubbingProperty(_parent, "same", "value");
        StubbingProperty p2 = new StubbingProperty(_parent, "same", "value");
        assertTrue(p1.isSame(p1));
        assertTrue(p1.isSame(p2));
    }

    @Test
    public void isSameFalse() throws RepositoryException {
        StubbingProperty p1 = new StubbingProperty(_parent, "same", "value");
        StubbingProperty p2 = new StubbingProperty(_parent, "same", "other");
        assertFalse(p1.isSame(p2));
    }

    @Test
    public void getPropertyNotImplemented() throws RepositoryException {
        assertThrows(NotImplementedException.class, () -> new StubbingProperty(_parent, "test", "value").getProperty());
    }

    @Test
    public void emptyValues() throws RepositoryException {
        StubbingProperty p = new StubbingProperty(_parent, "empty", (String[]) null);
        assertNull(p.getValue());
        assertEquals(1, p.getValues().length);
        assertNull(p.getValues()[0]);
        assertFalse(p.isMultiple());
    }

    @Test
    public void referenceIsMultiple() throws RepositoryException {
        Node n = NodeMockUtils.mockNode("test");
        StubbingProperty p = new StubbingProperty(_parent, "ref", n, n);
        assertTrue(p.isMultiple());
        assertEquals(n, p.getNode());
        assertEquals(2, p.getNodes().length);
    }

    @Test
    public void toStringTest() throws RepositoryException {
        StubbingProperty p = new StubbingProperty(_parent, "test", "value");
        String s = p.toString();
        assertNotNull(s);
        assertTrue(s.contains(StubbingProperty.class.getSimpleName()));
    }

    @Test
    public void isSameOtherItemFalse() throws RepositoryException {
        StubbingProperty p = new StubbingProperty(_parent, "test", "value");
        Node other = NodeMockUtils.mockNode("other");
        assertFalse(p.isSame(other));
    }

    @Test
    public void referenceSingleNotMultiple() throws RepositoryException {
        Node n = NodeMockUtils.mockNode("single");
        StubbingProperty p = new StubbingProperty(_parent, "ref", n);
        assertFalse(p.isMultiple());
        assertEquals(n, p.getNode());
        assertNotNull(p.getNodes());
        assertEquals(1, p.getNodes().length);
        assertEquals(n, p.getNodes()[0]);
    }

    @Test
    public void referenceLength() throws RepositoryException {
        Node n = NodeMockUtils.mockNode("len");
        StubbingProperty p = new StubbingProperty(_parent, "ref", n);
        assertEquals(PropertyType.REFERENCE, p.getType());
        assertEquals(n.getIdentifier(), p.getString());
        assertEquals(n.getIdentifier().length(), p.getLength());
    }
}

