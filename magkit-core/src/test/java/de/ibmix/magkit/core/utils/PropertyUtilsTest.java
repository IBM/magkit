package de.ibmix.magkit.core.utils;

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

import de.ibmix.magkit.test.jcr.NodeStubbingOperation;
import info.magnolia.jcr.decoration.ContentDecoratorPropertyWrapper;
import info.magnolia.jcr.wrapper.HTMLEscapingPropertyWrapper;
import org.apache.jackrabbit.JcrConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;

import static de.ibmix.magkit.core.utils.PropertyUtils.PROPERTY_NAME_COMPARATOR;
import static de.ibmix.magkit.core.utils.PropertyUtils.exists;
import static de.ibmix.magkit.core.utils.PropertyUtils.getBinaryValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getBinaryValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getBooleanValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getBooleanValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getCalendarValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getCalendarValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getDoubleValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getDoubleValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getLongValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getLongValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getProperties;
import static de.ibmix.magkit.core.utils.PropertyUtils.getProperty;
import static de.ibmix.magkit.core.utils.PropertyUtils.getStringValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getStringValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.retrieveMultiSelectProperties;
import static de.ibmix.magkit.core.utils.PropertyUtils.retrieveMultiSelectValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.retrieveOrderedMultiSelectValues;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.PropertyMockUtils.mockProperty;
import static de.ibmix.magkit.test.jcr.PropertyStubbingOperation.stubValues;
import static de.ibmix.magkit.test.jcr.ValueMockUtils.mockBinary;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

/**
 * Test for {@link PropertyUtils}.
 *
 * @author frank.sommer
 * @since 09.10.12
 */
public class PropertyUtilsTest {

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    @BeforeEach
    public void setUp() {
        cleanContext();
    }

    @Test
    public void propertyNameComparatorTest() throws RepositoryException {
        Property p1 = mockProperty("01-test");
        Property p2 = mockProperty("1-test");
        assertEquals(0, PROPERTY_NAME_COMPARATOR.compare(null, null));
        assertEquals(-7, PROPERTY_NAME_COMPARATOR.compare(null, p1));
        assertEquals(7, PROPERTY_NAME_COMPARATOR.compare(p1, null));
        assertEquals(0, PROPERTY_NAME_COMPARATOR.compare(p1, p1));
        assertEquals(-1, PROPERTY_NAME_COMPARATOR.compare(p1, p2));
        assertEquals(0, PROPERTY_NAME_COMPARATOR.compare(p2, p2));
        assertEquals(1, PROPERTY_NAME_COMPARATOR.compare(p2, p1));
    }

    @Test
    public void testRetrieveOrderedMultiSelectValues() throws Exception {
        assertEquals(0, retrieveOrderedMultiSelectValues(null).size());
        Node node = mockPageNode("/node/subNode",
            stubProperty("jcr:created", "2012"),
            stubProperty("1", "zwei1"),
            stubProperty("0", "eins0"),
            stubProperty("title", "title"),
            stubProperty("2", "drei2")
        );
        Collection<String> values = retrieveOrderedMultiSelectValues(node);
        assertEquals(3, values.size());
        int i = 0;
        for (String value : values) {
            assertTrue(value.endsWith(String.valueOf(i)));
            i++;
        }
    }

    @Test
    public void testRetrieveOrderedMultiSelectValuesSubNode() throws Exception {
        assertEquals(0, retrieveMultiSelectProperties(null, null).size());
        Node node = mockPageNode("/node");
        assertEquals(0, retrieveMultiSelectProperties(node, null).size());
        assertEquals(0, retrieveMultiSelectProperties(node, " ").size());
        assertEquals(0, retrieveMultiSelectProperties(node, "subNode").size());
        mockPageNode("/node/subNode",
            stubProperty("jcr:created", "2012"),
            stubProperty("1", "zwei1"),
            stubProperty("0", "eins0"),
            stubProperty("title", "title"),
            stubProperty("2", "drei2")
        );
        assertEquals(0, retrieveMultiSelectProperties(node, null).size());
        assertEquals(0, retrieveMultiSelectProperties(node, " ").size());
        Collection<String> values = retrieveOrderedMultiSelectValues(node, "subNode");
        assertEquals(3, values.size());
        int i = 0;
        for (String value : values) {
            assertTrue(value.endsWith(String.valueOf(i)));
            i++;
        }
    }

    @Test
    public void getPropertyTest() throws RepositoryException {
        assertNull(getProperty(null, null));
        Node node = mockNode("test");
        assertNull(getProperty(node, null));
        assertNull(getProperty(node, ""));
        assertNull(getProperty(node, "   "));
        assertNull(getProperty(node, "prop"));
        NodeStubbingOperation.stubProperty("prop", "test").of(node);
        assertNotNull(getProperty(node, "prop"));
        doThrow(RepositoryException.class).when(node).getProperty(anyString());
        assertNull(getProperty(node, "prop"));
    }

    @Test
    public void getStringValueTest() throws RepositoryException {
        assertNull(getStringValue(null));
        assertNull(getStringValue(mockProperty("test")));
        assertEquals("first", getStringValue(mockProperty("test", "first")));
        assertEquals("first", getStringValue(mockProperty("test", "first", "second")));
    }

    @Test
    public void getStringValueFromNode() throws RepositoryException {
        assertNull(getStringValue((Node) null, null));
        Node node = mockPageNode("test");
        assertNull(getStringValue(node, "name"));
        Property p = new HTMLEscapingPropertyWrapper(mockProperty("name", "<h1>test</h1>"), true);
        NodeStubbingOperation.stubProperty(p).of(node);
        assertEquals("&lt;h1&gt;test&lt;/h1&gt;", getStringValue(node, "name"));
    }

    @Test
    public void getStringValuesTestString() throws RepositoryException {
        assertEquals(0, getStringValues(null).size());
        Property p = mockProperty("test");
        assertEquals(0, getStringValues(p).size());
        p = mockProperty("test", "<h1>first</h1>");
        assertEquals(1, getStringValues(p).size());
        assertEquals("<h1>first</h1>", getStringValues(p).get(0));
        p = mockProperty("test", "<h1>first</h1>", null, "second");
        assertEquals(2, getStringValues(p).size());
        assertEquals("<h1>first</h1>", getStringValues(p).get(0));
        assertEquals("second", getStringValues(p).get(1));
    }

    @Test
    public void getStringValuesTestStringWithHtmlEncoding() throws RepositoryException {
        Property p = new HTMLEscapingPropertyWrapper(mockProperty("test", "<h1>test</h1>"), true);
        assertEquals(1, getStringValues(p).size());
        assertEquals("&lt;h1&gt;test&lt;/h1&gt;", getStringValues(p).get(0));
        p = new HTMLEscapingPropertyWrapper(mockProperty("test", null, "<h1>test</h1>", "<h2>test</h2>"), true);
        assertEquals(2, getStringValues(p).size());
        assertEquals("&lt;h1&gt;test&lt;/h1&gt;", getStringValues(p).get(0));
        assertEquals("&lt;h2&gt;test&lt;/h2&gt;", getStringValues(p).get(1));
    }

    @Test
    public void getStringValuesTestDouble() throws RepositoryException {
        Property p = mockProperty("test", 2.3D);
        assertEquals("2.3", getStringValues(p).get(0));
    }

    @Test
    public void getStringValuesTestLong() throws RepositoryException {
        Property p = mockProperty("test", 23L);
        assertEquals("23", getStringValues(p).get(0));
    }

    @Test
    public void getStringValuesTestBoolean() throws RepositoryException {
        Property p = mockProperty("test", true, false);
        assertEquals("true", getStringValues(p).get(0));
        assertEquals("false", getStringValues(p).get(1));
    }

    @Test
    public void getStringValuesTestCalendar() throws RepositoryException {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.setTimeZone(TimeZone.getTimeZone("UK/London"));
        Property p = mockProperty("test", cal);
        assertEquals("1970-01-01T00:00:00.000Z", getStringValues(p).get(0));
    }

    @Test
    public void getCalenderValueTest() throws RepositoryException {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        assertNull(getCalendarValue(null));
        assertNull(getCalendarValue(mockProperty("test")));
        assertEquals(cal1, getCalendarValue(mockProperty("test", cal1)));
        assertEquals(cal1, getCalendarValue(mockProperty("test", cal1, cal2)));
    }

    @Test
    public void getCalendarValueFromNode() throws RepositoryException {
        assertNull(getCalendarValue((Node) null, null));
        Node node = mockPageNode("test");
        assertNull(getCalendarValue(node, "date"));
        Calendar date = Calendar.getInstance();
        NodeStubbingOperation.stubProperty("date", date).of(node);
        assertEquals(date, getCalendarValue(node, "date"));
    }

    @Test
    public void getCalenderValuesTest() throws RepositoryException {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        assertEquals(0, getCalendarValues(null).size());
        assertEquals(0, getCalendarValues(mockProperty("test")).size());
        assertEquals(1, getCalendarValues(mockProperty("test", cal1)).size());
        assertEquals(2, getCalendarValues(mockProperty("test", cal1, null, cal2)).size());
        assertEquals(cal1, getCalendarValues(mockProperty("test", cal1, cal2)).get(0));
        assertEquals(cal2, getCalendarValues(mockProperty("test", cal1, cal2)).get(1));
    }

    @Test
    public void getCalendarValuesFromNode() throws RepositoryException {
        assertEquals(0, getCalendarValues((Node) null, null).size());
        Node node = mockPageNode("test");
        assertEquals(0, getCalendarValues(node, null).size());
        assertEquals(0, getCalendarValues(node, "date").size());
        Calendar date1 = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        NodeStubbingOperation.stubProperty("date", date1, date2).of(node);
        assertEquals(2, getCalendarValues(node, "date").size());
        assertEquals(date1, getCalendarValues(node, "date").get(0));
        assertEquals(date2, getCalendarValues(node, "date").get(1));
    }

    @Test
    public void getDoubleValueTest() throws RepositoryException {
        assertNull(getDoubleValue(null));
        assertNull(getDoubleValue(mockProperty("test")));
        assertEquals(1.1D, getDoubleValue(mockProperty("test", 1.1D)));
        assertEquals(1.1D, getDoubleValue(mockProperty("test", 1.1D, 1.2D)));
    }

    @Test
    public void getDoubleValueFromNode() throws RepositoryException {
        assertNull(PropertyUtils.getDoubleValue((Node) null, null));
        Node node = mockPageNode("test");
        assertNull(PropertyUtils.getDoubleValue(node, "double"));
        NodeStubbingOperation.stubProperty("double", 1.23D).of(node);
        assertEquals(1.23D, PropertyUtils.getDoubleValue(node, "double"));
    }

    @Test
    public void getDoubleValuesFromNode() throws RepositoryException {
        assertEquals(0, PropertyUtils.getDoubleValues((Node) null, null).size());
        Node node = mockPageNode("test");
        assertEquals(0, PropertyUtils.getDoubleValues(node, "double").size());
        NodeStubbingOperation.stubProperty("double", 1.23D, 4.56D).of(node);
        assertEquals(2, PropertyUtils.getDoubleValues(node, "double").size());
        assertEquals(1.23D, PropertyUtils.getDoubleValues(node, "double").get(0));
        assertEquals(4.56D, PropertyUtils.getDoubleValues(node, "double").get(1));
    }

    @Test
    public void getDoubleValuesTest() throws RepositoryException {
        assertEquals(0, getDoubleValues(null).size());
        assertEquals(0, getDoubleValues(mockProperty("test")).size());
        assertEquals(1, getDoubleValues(mockProperty("test", 2.2)).size());
        assertEquals(2, getDoubleValues(mockProperty("test", 2.3D, 1.0D)).size());
        assertEquals(2.3D, getDoubleValues(mockProperty("test", 2.3D, 0.6D)).get(0));
        assertEquals(0.6D, getDoubleValues(mockProperty("test", 2.3D, 0.6D)).get(1));
    }

    @Test
    public void getLongValueTest() throws RepositoryException {
        assertNull(getLongValue(null));
        assertNull(getLongValue(mockProperty("test")));
        assertEquals(1L, getLongValue(mockProperty("test", 1L)));
        assertEquals(6L, getLongValue(mockProperty("test", 6L, 1L)));
    }

    @Test
    public void getLongValueFromNode() throws RepositoryException {
        assertNull(PropertyUtils.getLongValue((Node) null, null));
        Node node = mockPageNode("test");
        assertNull(PropertyUtils.getLongValue(node, "long"));
        NodeStubbingOperation.stubProperty("long", 123L).of(node);
        assertEquals(123L, PropertyUtils.getLongValue(node, "long"));
    }

    @Test
    public void getLongValuesFromNode() throws RepositoryException {
        assertEquals(0, PropertyUtils.getLongValues((Node) null, null).size());
        Node node = mockPageNode("test");
        assertEquals(0, PropertyUtils.getLongValues(node, "long").size());
        NodeStubbingOperation.stubProperty("long", 123L, 456L).of(node);
        assertEquals(2, PropertyUtils.getLongValues(node, "long").size());
        assertEquals(123L, PropertyUtils.getLongValues(node, "long").get(0));
        assertEquals(456L, PropertyUtils.getLongValues(node, "long").get(1));
    }

    @Test
    public void getLongValuesTest() throws RepositoryException {
        assertEquals(0, getLongValues(null).size());
        assertEquals(0, getLongValues(mockProperty("test")).size());
        assertEquals(1, getLongValues(mockProperty("test", 2L)).size());
        assertEquals(2, getLongValues(mockProperty("test", 2L, 1L)).size());
        assertEquals(2L, getLongValues(mockProperty("test", 2L, 0L)).get(0));
        assertEquals(0L, getLongValues(mockProperty("test", 2L, 0L)).get(1));
    }

    @Test
    public void getBooleanValueTest() throws RepositoryException {
        assertNull(getBooleanValue(null));
        assertNull(getBooleanValue(mockProperty("test")));
        assertFalse(getBooleanValue(mockProperty("test", false)));
        assertTrue(getBooleanValue(mockProperty("test", true, false)));
    }

    @Test
    public void getBooleanValueFromNode() throws RepositoryException {
        assertNull(PropertyUtils.getBooleanValue((Node) null, null));
        Node node = mockPageNode("test");
        assertNull(PropertyUtils.getBooleanValue(node, "boolean"));
        NodeStubbingOperation.stubProperty("boolean", true).of(node);
        assertTrue(PropertyUtils.getBooleanValue(node, "boolean"));
    }

    @Test
    public void getBooleanValuesFromNode() throws RepositoryException {
        assertEquals(0, PropertyUtils.getBooleanValues((Node) null, null).size());
        Node node = mockPageNode("test");
        assertEquals(0, PropertyUtils.getBooleanValues(node, "boolean").size());
        NodeStubbingOperation.stubProperty("boolean", true, false).of(node);
        assertEquals(2, PropertyUtils.getBooleanValues(node, "boolean").size());
        assertTrue(PropertyUtils.getBooleanValues(node, "boolean").get(0));
        assertFalse(PropertyUtils.getBooleanValues(node, "boolean").get(1));
    }

    @Test
    public void getBooleanValuesTest() throws RepositoryException {
        assertEquals(0, getBooleanValues(null).size());
        assertEquals(0, getBooleanValues(mockProperty("test")).size());
        assertEquals(1, getBooleanValues(mockProperty("test", true)).size());
        assertEquals(2, getBooleanValues(mockProperty("test", false, true)).size());
        assertFalse(getBooleanValues(mockProperty("test", false, true)).get(0));
        assertTrue(getBooleanValues(mockProperty("test", false, true)).get(1));
    }

    @Test
    public void getBinaryValueTest() throws RepositoryException {
        Binary a = mockBinary("a");
        Binary b = mockBinary("b");
        assertNull(getBinaryValue(null));
        assertNull(getBinaryValue(mockProperty("test")));
        assertEquals(b, getBinaryValue(mockProperty("test", b)));
        assertEquals(a, getBinaryValue(mockProperty("test", a, b)));
    }

    @Test
    public void getBinaryValueFromNode() throws RepositoryException {
        assertNull(PropertyUtils.getBinaryValue((Node) null, null));
        Node node = mockPageNode("test");
        assertNull(PropertyUtils.getBinaryValue(node, "binary"));
        Binary a = mockBinary("a");
        NodeStubbingOperation.stubProperty("binary", a).of(node);
        assertEquals(a, PropertyUtils.getBinaryValue(node, "binary"));
    }

    @Test
    public void getBinaryValuesFromNode() throws RepositoryException {
        assertEquals(0, PropertyUtils.getBinaryValues((Node) null, null).size());
        Node node = mockPageNode("test");
        assertEquals(0, PropertyUtils.getBinaryValues(node, "binary").size());
        Binary a = mockBinary("a");
        Binary b = mockBinary("b");
        NodeStubbingOperation.stubProperty("binary", a, b).of(node);
        assertEquals(2, PropertyUtils.getBinaryValues(node, "binary").size());
        assertEquals(a, PropertyUtils.getBinaryValues(node, "binary").get(0));
        assertEquals(b, PropertyUtils.getBinaryValues(node, "binary").get(1));
    }

    @Test
    public void getBinaryValuesTest() throws RepositoryException {
        Binary a = mockBinary("a");
        Binary b = mockBinary("b");
        assertEquals(0, getBinaryValues(null).size());
        assertEquals(0, getBinaryValues(mockProperty("test")).size());
        assertEquals(1, getBinaryValues(mockProperty("test", b)).size());
        assertEquals(2, getBinaryValues(mockProperty("test", b, a)).size());
        assertEquals(b, getBinaryValues(mockProperty("test", b, a)).get(0));
        assertEquals(a, getBinaryValues(mockProperty("test", b, a)).get(1));
    }

    @Test
    public void getValue() throws RepositoryException {
        assertNull(PropertyUtils.getValue(null));
        Property p = mockProperty("test");
        assertNull(PropertyUtils.getValue(p));
        stubValues(new Value[0]).of(p);
        doReturn(true).when(p).isMultiple();
        assertNull(PropertyUtils.getValue(p));
        stubValues("first").of(p);
        assertEquals("first", PropertyUtils.getValue(p).getString());
        stubValues("first", "second", "third").of(p);
        assertEquals("first", PropertyUtils.getValue(p).getString());
        Property wrapper = new ContentDecoratorPropertyWrapper<>(null, null);
        assertNull(PropertyUtils.getValue(wrapper));
    }

    // --- Additional tests for uncovered branches and fallbacks ---

    @Test
    public void existsMethodVariants() throws RepositoryException {
        assertFalse(exists(null));
        Property plain = mockProperty("plain", "x");
        assertTrue(exists(plain));
        Property emptyWrapper = new ContentDecoratorPropertyWrapper<>(null, null);
        assertFalse(exists(emptyWrapper));
    }

    @Test
    public void retrieveMultiSelectValuesVariants() throws RepositoryException {
        Node node = mockPageNode("/multi",
            stubProperty("2", "zwei"),
            stubProperty("0", "eins"),
            stubProperty("1", "drei"),
            stubProperty("title", "ignore")
        );
        Collection<String> unordered = retrieveMultiSelectValues(node);
        assertEquals(3, unordered.size());
        assertTrue(unordered.contains("eins") && unordered.contains("zwei") && unordered.contains("drei"));
        Node parent = mockPageNode("/parent");
        mockPageNode("/parent/sub",
            stubProperty("0", "a"),
            stubProperty("1", "b"),
            stubProperty("5", "c")
        );
        Collection<String> bySub = retrieveMultiSelectValues(parent, "sub");
        assertEquals(3, bySub.size());
        assertTrue(bySub.contains("a") && bySub.contains("b") && bySub.contains("c"));
    }

    @Test
    public void fallbackGetterVariants() throws RepositoryException {
        Node node = mockPageNode("/fallback");
        assertEquals("fb", PropertyUtils.getStringValue(node, "missing", "fb"));
        Calendar fbCal = Calendar.getInstance();
        assertEquals(fbCal, PropertyUtils.getCalendarValue(node, "missingCal", fbCal));
        assertEquals(42L, PropertyUtils.getLongValue(node, "missingLong", 42L));
        assertEquals(3.14D, PropertyUtils.getDoubleValue(node, "missingDouble", 3.14D));
        assertTrue(PropertyUtils.getBooleanValue(node, "missingBool", Boolean.TRUE));
        Binary fbBin = mockBinary("bin");
        assertEquals(fbBin, PropertyUtils.getBinaryValue(node, "missingBin", fbBin));
        NodeStubbingOperation.stubProperty("long", 123L).of(node);
        assertEquals(123L, PropertyUtils.getLongValue(node, "long", 999L));
        NodeStubbingOperation.stubProperty("str", "value").of(node);
        assertEquals("value", PropertyUtils.getStringValue(node, "str", "other"));
    }

    @Test
    public void getPropertiesVariants() throws RepositoryException {
        assertNull(getProperties((Node) null));
        assertNull(getProperties((Node) null, "*"));
        assertNull(getProperties((Node) null, new String[]{"*"}));
        Node node = mockNode(stubProperty("prop", "v"));
        PropertyIterator iter = PropertyUtils.getProperties(node);
        assertEquals(node.getProperty(JcrConstants.JCR_PRIMARYTYPE), iter.nextProperty());
        assertEquals(node.getProperty("prop"), iter.nextProperty());
        iter = PropertyUtils.getProperties(node, "prop*");
        assertEquals(node.getProperty(JcrConstants.JCR_PRIMARYTYPE), iter.nextProperty());
        assertEquals(node.getProperty("prop"), iter.nextProperty());
        iter = PropertyUtils.getProperties(node, new String[]{"prop*"});
        assertEquals(node.getProperty(JcrConstants.JCR_PRIMARYTYPE), iter.nextProperty());
        assertEquals(node.getProperty("prop"), iter.nextProperty());
        doThrow(new RepositoryException()).when(node).getProperties();
        doThrow(new RepositoryException()).when(node).getProperties(anyString());
        doThrow(new RepositoryException()).when(node).getProperties(any(String[].class));
        assertNull(getProperties(node));
        assertNull(getProperties(node, "*"));
        assertNull(getProperties(node, new String[]{"*"}));
    }
}
