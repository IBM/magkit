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
import info.magnolia.jcr.wrapper.HTMLEscapingPropertyWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;

import static de.ibmix.magkit.core.utils.PropertyUtils.PROPERTY_NAME_COMPARATOR;
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
import static de.ibmix.magkit.core.utils.PropertyUtils.getProperty;
import static de.ibmix.magkit.core.utils.PropertyUtils.getStringValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getStringValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.retrieveMultiSelectProperties;
import static de.ibmix.magkit.core.utils.PropertyUtils.retrieveOrderedMultiSelectValues;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.PropertyMockUtils.mockProperty;
import static de.ibmix.magkit.test.jcr.ValueMockUtils.mockBinary;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

/**
 * Test for {@link PropertyUtils}.
 *
 * @author frank.sommer
 * @since 09.10.12
 */
public class PropertyUtilsTest {

    @After
    public void tearDown() {
        cleanContext();
    }

    @Before
    public void setUp() {
        cleanContext();
    }

    @Test
    public void propertyNameComparatorTest() throws RepositoryException {
        Property p1 = mockProperty("01-test");
        Property p2 = mockProperty("1-test");
        assertThat(PROPERTY_NAME_COMPARATOR.compare(null, null), is(0));
        assertThat(PROPERTY_NAME_COMPARATOR.compare(null, p1), is(-7));
        assertThat(PROPERTY_NAME_COMPARATOR.compare(p1, null), is(7));
        assertThat(PROPERTY_NAME_COMPARATOR.compare(p1, p1), is(0));
        assertThat(PROPERTY_NAME_COMPARATOR.compare(p1, p2), is(-1));
        assertThat(PROPERTY_NAME_COMPARATOR.compare(p2, p2), is(0));
        assertThat(PROPERTY_NAME_COMPARATOR.compare(p2, p1), is(1));
    }

    @Test
    public void testRetrieveOrderedMultiSelectValues() throws Exception {
        assertThat(retrieveOrderedMultiSelectValues(null).size(), is(0));

        Node node = mockPageNode("/node/subNode",
            stubProperty("jcr:created", "2012"),
            stubProperty("1", "zwei1"),
            stubProperty("0", "eins0"),
            stubProperty("title", "title"),
            stubProperty("2", "drei2")
        );
        Collection<String> values = retrieveOrderedMultiSelectValues(node);
        assertThat(values.size(), is(3));
        int i = 0;
        for (String value : values) {
            assertThat(value.endsWith(String.valueOf(i)), is(true));
            i++;
        }
    }

    @Test
    public void testRetrieveOrderedMultiSelectValuesSubNode() throws Exception {
        assertThat(retrieveMultiSelectProperties(null, null).size(), is(0));

        Node node = mockPageNode("/node");
        assertThat(retrieveMultiSelectProperties(node, null).size(), is(0));
        assertThat(retrieveMultiSelectProperties(node, " ").size(), is(0));
        assertThat(retrieveMultiSelectProperties(node, "subNode").size(), is(0));

        mockPageNode("/node/subNode",
            stubProperty("jcr:created", "2012"),
            stubProperty("1", "zwei1"),
            stubProperty("0", "eins0"),
            stubProperty("title", "title"),
            stubProperty("2", "drei2")
        );
        assertThat(retrieveMultiSelectProperties(node, null).size(), is(0));
        assertThat(retrieveMultiSelectProperties(node, " ").size(), is(0));
        Collection<String> values = retrieveOrderedMultiSelectValues(node, "subNode");
        assertThat(values.size(), is(3));
        int i = 0;
        for (String value : values) {
            assertThat(value.endsWith(String.valueOf(i)), is(true));
            i++;
        }
    }

    @Test
    public void getPropertyTest() throws RepositoryException {
        assertThat(getProperty(null, null), nullValue());

        Node node = mockNode("test");
        assertThat(getProperty(node, null), nullValue());
        assertThat(getProperty(node, ""), nullValue());
        assertThat(getProperty(node, "   "), nullValue());
        assertThat(getProperty(node, "prop"), nullValue());

        NodeStubbingOperation.stubProperty("prop", "test").of(node);
        assertThat(getProperty(node, "prop"), notNullValue());

        doThrow(RepositoryException.class).when(node).getProperty(anyString());
        assertThat(getProperty(node, "prop"), nullValue());
    }

    @Test
    public void getStringValueTest() throws RepositoryException {
        assertThat(getStringValue(null), nullValue());
        assertThat(getStringValue(mockProperty("test")), nullValue());
        assertThat(getStringValue(mockProperty("test", "first")), is("first"));
        assertThat(getStringValue(mockProperty("test", "first", "second")), is("first"));
    }

    @Test
    public void getStringValueFromNode() throws RepositoryException {
        assertThat(getStringValue((Node) null, null), nullValue());

        Node node = mockPageNode("test");
        assertThat(getStringValue(node, "name"), nullValue());

        Property p = new HTMLEscapingPropertyWrapper(mockProperty("name", "<h1>test</h1>"), true);
        NodeStubbingOperation.stubProperty(p).of(node);
        assertThat(getStringValue(node, "name"), is("&lt;h1&gt;test&lt;/h1&gt;"));
    }

    @Test
    public void getStringValuesTestString() throws RepositoryException {
        assertThat(getStringValues(null).size(), is(0));

        Property p = mockProperty("test");
        assertThat(getStringValues(p).size(), is(0));

        p = mockProperty("test", "<h1>first</h1>");
        assertThat(getStringValues(p).size(), is(1));
        assertThat(getStringValues(p).get(0), is("<h1>first</h1>"));

        p = mockProperty("test", "<h1>first</h1>", null, "second");
        assertThat(getStringValues(p).size(), is(2));
        assertThat(getStringValues(p).get(0), is("<h1>first</h1>"));
        assertThat(getStringValues(p).get(1), is("second"));
    }

    @Test
    public void getStringValuesTestStringWithHtmlEncoding() throws RepositoryException {
        Property p = new HTMLEscapingPropertyWrapper(mockProperty("test", "<h1>test</h1>"), true);
        assertThat(getStringValues(p).size(), is(1));
        assertThat(getStringValues(p).get(0), is("&lt;h1&gt;test&lt;/h1&gt;"));

        p = new HTMLEscapingPropertyWrapper(mockProperty("test", null, "<h1>test</h1>", "<h2>test</h2>"), true);
        assertThat(getStringValues(p).size(), is(2));
        assertThat(getStringValues(p).get(0), is("&lt;h1&gt;test&lt;/h1&gt;"));
        assertThat(getStringValues(p).get(1), is("&lt;h2&gt;test&lt;/h2&gt;"));
    }

    @Test
    public void getStringValuesTestDouble() throws RepositoryException {
        Property p = mockProperty("test", 2.3D);
        assertThat(getStringValues(p).get(0), is("2.3"));
    }

    @Test
    public void getStringValuesTestLong() throws RepositoryException {
        Property p = mockProperty("test", 23L);
        assertThat(getStringValues(p).get(0), is("23"));
    }

    @Test
    public void getStringValuesTestBoolean() throws RepositoryException {
        Property p = mockProperty("test", true, false);
        assertThat(getStringValues(p).get(0), is("true"));
        assertThat(getStringValues(p).get(1), is("false"));
    }

    @Test
    public void getStringValuesTestCalendar() throws RepositoryException {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.setTimeZone(TimeZone.getTimeZone("UK/London"));
        Property p = mockProperty("test", cal);
        assertThat(getStringValues(p).get(0), is("1970-01-01T00:00:00.000Z"));
    }

    @Test
    public void getCalenderValueTest() throws RepositoryException {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        assertThat(getCalendarValue(null), nullValue());
        assertThat(getCalendarValue(mockProperty("test")), nullValue());
        assertThat(getCalendarValue(mockProperty("test", cal1)), is(cal1));
        assertThat(getCalendarValue(mockProperty("test", cal1, cal2)), is(cal1));
    }

    @Test
    public void getCalendarValueFromNode() throws RepositoryException {
        assertThat(getCalendarValue((Node) null, null), nullValue());

        Node node = mockPageNode("test");
        assertThat(getCalendarValue(node, "date"), nullValue());

        Calendar date = Calendar.getInstance();
        NodeStubbingOperation.stubProperty("date", date).of(node);
        assertThat(getCalendarValue(node, "date"), is(date));
    }

    @Test
    public void getCalenderValuesTest() throws RepositoryException {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        assertThat(getCalendarValues(null).size(), is(0));
        assertThat(getCalendarValues(mockProperty("test")).size(), is(0));
        assertThat(getCalendarValues(mockProperty("test", cal1)).size(), is(1));
        assertThat(getCalendarValues(mockProperty("test", cal1, null, cal2)).size(), is(2));
        assertThat(getCalendarValues(mockProperty("test", cal1, cal2)).get(0), is(cal1));
        assertThat(getCalendarValues(mockProperty("test", cal1, cal2)).get(1), is(cal2));
    }

    @Test
    public void getCalendarValuesFromNode() throws RepositoryException {
        assertThat(getCalendarValues((Node) null, null), notNullValue());
        assertThat(getCalendarValues((Node) null, null).size(), is(0));

        Node node = mockPageNode("test");
        assertThat(getCalendarValues(node, null).size(), is(0));
        assertThat(getCalendarValues(node, "date").size(), is(0));

        Calendar date1 = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        NodeStubbingOperation.stubProperty("date", date1, date2).of(node);
        assertThat(getCalendarValues(node, "date").size(), is(2));
        assertThat(getCalendarValues(node, "date").get(0), is(date1));
        assertThat(getCalendarValues(node, "date").get(1), is(date2));
    }

    @Test
    public void getDoubleValueTest() throws RepositoryException {
        assertThat(getDoubleValue(null), nullValue());
        assertThat(getDoubleValue(mockProperty("test")), nullValue());
        assertThat(getDoubleValue(mockProperty("test", 1.1D)), is(1.1D));
        assertThat(getDoubleValue(mockProperty("test", 1.1D, 1.2D)), is(1.1D));
    }

    @Test
    public void getDoubleValueFromNode() throws RepositoryException {
        assertThat(PropertyUtils.getDoubleValue((Node) null, null), nullValue());

        Node node = mockPageNode("test");
        assertThat(PropertyUtils.getDoubleValue(node, "double"), nullValue());

        NodeStubbingOperation.stubProperty("double", 1.23D).of(node);
        assertThat(PropertyUtils.getDoubleValue(node, "double"), is(1.23D));
    }

    @Test
    public void getDoubleValuesFromNode() throws RepositoryException {
        assertThat(PropertyUtils.getDoubleValues((Node) null, null), notNullValue());
        assertThat(PropertyUtils.getDoubleValues((Node) null, null).size(), is(0));

        Node node = mockPageNode("test");
        assertThat(PropertyUtils.getDoubleValues(node, "double").size(), is(0));

        NodeStubbingOperation.stubProperty("double", 1.23D, 4.56D).of(node);
        assertThat(PropertyUtils.getDoubleValues(node, "double").size(), is(2));
        assertThat(PropertyUtils.getDoubleValues(node, "double").get(0), is(1.23D));
        assertThat(PropertyUtils.getDoubleValues(node, "double").get(1), is(4.56D));
    }

    @Test
    public void getDoubleValuesTest() throws RepositoryException {
        assertThat(getDoubleValues(null).size(), is(0));
        assertThat(getDoubleValues(mockProperty("test")).size(), is(0));
        assertThat(getDoubleValues(mockProperty("test", 2.2)).size(), is(1));
        assertThat(getDoubleValues(mockProperty("test", 2.3D, 1.0D)).size(), is(2));
        assertThat(getDoubleValues(mockProperty("test", 2.3D, 0.6D)).get(0), is(2.3D));
        assertThat(getDoubleValues(mockProperty("test", 2.3D, 0.6D)).get(1), is(0.6D));
    }

    @Test
    public void getLongValueTest() throws RepositoryException {
        assertThat(getLongValue(null), nullValue());
        assertThat(getLongValue(mockProperty("test")), nullValue());
        assertThat(getLongValue(mockProperty("test", 1L)), is(1L));
        assertThat(getLongValue(mockProperty("test", 6L, 1L)), is(6L));
    }

    @Test
    public void getLongValueFromNode() throws RepositoryException {
        assertThat(PropertyUtils.getLongValue((Node) null, null), nullValue());

        Node node = mockPageNode("test");
        assertThat(PropertyUtils.getLongValue(node, "long"), nullValue());

        NodeStubbingOperation.stubProperty("long", 123L).of(node);
        assertThat(PropertyUtils.getLongValue(node, "long"), is(123L));
    }

    @Test
    public void getLongValuesFromNode() throws RepositoryException {
        assertThat(PropertyUtils.getLongValues((Node) null, null), notNullValue());
        assertThat(PropertyUtils.getLongValues((Node) null, null).size(), is(0));

        Node node = mockPageNode("test");
        assertThat(PropertyUtils.getLongValues(node, "long").size(), is(0));

        NodeStubbingOperation.stubProperty("long", 123L, 456L).of(node);
        assertThat(PropertyUtils.getLongValues(node, "long").size(), is(2));
        assertThat(PropertyUtils.getLongValues(node, "long").get(0), is(123L));
        assertThat(PropertyUtils.getLongValues(node, "long").get(1), is(456L));
    }

    @Test
    public void getLongValuesTest() throws RepositoryException {
        assertThat(getLongValues(null).size(), is(0));
        assertThat(getLongValues(mockProperty("test")).size(), is(0));
        assertThat(getLongValues(mockProperty("test", 2L)).size(), is(1));
        assertThat(getLongValues(mockProperty("test", 2L, 1L)).size(), is(2));
        assertThat(getLongValues(mockProperty("test", 2L, 0L)).get(0), is(2L));
        assertThat(getLongValues(mockProperty("test", 2L, 0L)).get(1), is(0L));
    }

    @Test
    public void getBooleanValueTest() throws RepositoryException {
        assertThat(getBooleanValue(null), nullValue());
        assertThat(getBooleanValue(mockProperty("test")), nullValue());
        assertThat(getBooleanValue(mockProperty("test", false)), is(false));
        assertThat(getBooleanValue(mockProperty("test", true, false)), is(true));
    }

    @Test
    public void getBooleanValueFromNode() throws RepositoryException {
        assertThat(PropertyUtils.getBooleanValue((Node) null, null), nullValue());

        Node node = mockPageNode("test");
        assertThat(PropertyUtils.getBooleanValue(node, "boolean"), nullValue());

        NodeStubbingOperation.stubProperty("boolean", true).of(node);
        assertThat(PropertyUtils.getBooleanValue(node, "boolean"), is(true));
    }

    @Test
    public void getBooleanValuesFromNode() throws RepositoryException {
        assertThat(PropertyUtils.getBooleanValues((Node) null, null), notNullValue());
        assertThat(PropertyUtils.getBooleanValues((Node) null, null).size(), is(0));

        Node node = mockPageNode("test");
        assertThat(PropertyUtils.getBooleanValues(node, "boolean").size(), is(0));

        NodeStubbingOperation.stubProperty("boolean", true, false).of(node);
        assertThat(PropertyUtils.getBooleanValues(node, "boolean").size(), is(2));
        assertThat(PropertyUtils.getBooleanValues(node, "boolean").get(0), is(true));
        assertThat(PropertyUtils.getBooleanValues(node, "boolean").get(1), is(false));
    }

    @Test
    public void getBooleanValuesTest() throws RepositoryException {
        assertThat(getBooleanValues(null).size(), is(0));
        assertThat(getBooleanValues(mockProperty("test")).size(), is(0));
        assertThat(getBooleanValues(mockProperty("test", true)).size(), is(1));
        assertThat(getBooleanValues(mockProperty("test", false, true)).size(), is(2));
        assertThat(getBooleanValues(mockProperty("test", false, true)).get(0), is(false));
        assertThat(getBooleanValues(mockProperty("test", false, true)).get(1), is(true));
    }

    @Test
    public void getBinaryValueTest() throws RepositoryException {
        Binary a = mockBinary("a");
        Binary b = mockBinary("b");
        assertThat(getBinaryValue(null), nullValue());
        assertThat(getBinaryValue(mockProperty("test")), nullValue());
        assertThat(getBinaryValue(mockProperty("test", b)), is(b));
        assertThat(getBinaryValue(mockProperty("test", a, b)), is(a));
    }

    @Test
    public void getBinaryValueFromNode() throws RepositoryException {
        assertThat(PropertyUtils.getBinaryValue((Node) null, null), nullValue());

        Node node = mockPageNode("test");
        assertThat(PropertyUtils.getBinaryValue(node, "binary"), nullValue());

        Binary a = mockBinary("a");
        NodeStubbingOperation.stubProperty("binary", a).of(node);
        assertThat(PropertyUtils.getBinaryValue(node, "binary"), is(a));
    }

    @Test
    public void getBinaryValuesFromNode() throws RepositoryException {
        assertThat(PropertyUtils.getBinaryValues((Node) null, null), notNullValue());
        assertThat(PropertyUtils.getBinaryValues((Node) null, null).size(), is(0));

        Node node = mockPageNode("test");
        assertThat(PropertyUtils.getBinaryValues(node, "binary").size(), is(0));

        Binary a = mockBinary("a");
        Binary b = mockBinary("b");
        NodeStubbingOperation.stubProperty("binary", a, b).of(node);
        assertThat(PropertyUtils.getBinaryValues(node, "binary").size(), is(2));
        assertThat(PropertyUtils.getBinaryValues(node, "binary").get(0), is(a));
        assertThat(PropertyUtils.getBinaryValues(node, "binary").get(1), is(b));
    }

    @Test
    public void getBinaryValuesTest() throws RepositoryException {
        Binary a = mockBinary("a");
        Binary b = mockBinary("b");
        assertThat(getBinaryValues(null).size(), is(0));
        assertThat(getBinaryValues(mockProperty("test")).size(), is(0));
        assertThat(getBinaryValues(mockProperty("test", b)).size(), is(1));
        assertThat(getBinaryValues(mockProperty("test", b, a)).size(), is(2));
        assertThat(getBinaryValues(mockProperty("test", b, a)).get(0), is(b));
        assertThat(getBinaryValues(mockProperty("test", b, a)).get(1), is(a));
    }
}
