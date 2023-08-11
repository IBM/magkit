package com.aperto.magkit.utils;

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.mockito.jcr.NodeStubbingOperation;
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

import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static com.aperto.magkit.mockito.jcr.PropertyMockUtils.mockProperty;
import static com.aperto.magkit.mockito.jcr.ValueMockUtils.mockBinary;
import static com.aperto.magkit.utils.PropertyUtils.PROPERTY_NAME_COMPARATOR;
import static com.aperto.magkit.utils.PropertyUtils.getBinaryValue;
import static com.aperto.magkit.utils.PropertyUtils.getBinaryValues;
import static com.aperto.magkit.utils.PropertyUtils.getBooleanValue;
import static com.aperto.magkit.utils.PropertyUtils.getBooleanValues;
import static com.aperto.magkit.utils.PropertyUtils.getCalendarValue;
import static com.aperto.magkit.utils.PropertyUtils.getCalendarValues;
import static com.aperto.magkit.utils.PropertyUtils.getDoubleValue;
import static com.aperto.magkit.utils.PropertyUtils.getDoubleValues;
import static com.aperto.magkit.utils.PropertyUtils.getLongValue;
import static com.aperto.magkit.utils.PropertyUtils.getLongValues;
import static com.aperto.magkit.utils.PropertyUtils.getProperty;
import static com.aperto.magkit.utils.PropertyUtils.getStringValue;
import static com.aperto.magkit.utils.PropertyUtils.getStringValues;
import static com.aperto.magkit.utils.PropertyUtils.retrieveMultiSelectProperties;
import static com.aperto.magkit.utils.PropertyUtils.retrieveOrderedMultiSelectValues;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;

/**
 * Test for {@link PropertyUtils}.
 *
 * @author frank.sommer
 * @since 09.10.12
 */
public class PropertyUtilsTest {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @After
    public void tearDown() {
        ContextMockUtils.cleanContext();
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
        Property p = mockProperty("test", cal);
        assertThat(getStringValues(p).get(0), is("1970-01-01T01:00:00.000+01:00"));
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
    public void getDoubleValueTest() throws RepositoryException {
        assertThat(getDoubleValue(null), nullValue());
        assertThat(getDoubleValue(mockProperty("test")), nullValue());
        assertThat(getDoubleValue(mockProperty("test", 1.1D)), is(1.1D));
        assertThat(getDoubleValue(mockProperty("test", 1.1D, 1.2D)), is(1.1D));
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
