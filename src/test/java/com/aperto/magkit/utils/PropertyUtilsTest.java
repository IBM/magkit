package com.aperto.magkit.utils;

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.mockito.jcr.NodeStubbingOperation;
import info.magnolia.jcr.wrapper.HTMLEscapingPropertyWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.Collection;

import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static com.aperto.magkit.mockito.jcr.PropertyMockUtils.mockProperty;
import static com.aperto.magkit.utils.PropertyUtils.getProperty;
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
    public void testRetrieveOrderedMultiSelectValues() throws Exception {
        assertThat(retrieveMultiSelectProperties(null), nullValue());

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
    public void getStringValuesTestFromString() throws RepositoryException {
        assertThat(getStringValues(null).size(), is(0));

        Property p = mockProperty("test");
        assertThat(getStringValues(p).size(), is(1));
        assertThat(getStringValues(p).get(0), nullValue());

        p = mockProperty("test", "first");
        assertThat(getStringValues(p).size(), is(1));
        assertThat(getStringValues(p).get(0), is("first"));

        p = mockProperty("test", "first", "second");
        assertThat(getStringValues(p).size(), is(2));
        assertThat(getStringValues(p).get(0), is("first"));
        assertThat(getStringValues(p).get(1), is("second"));
    }

    @Test
    public void getStringValuesTestFromStringWithHtmlEncoding() throws RepositoryException {
        Property p = new HTMLEscapingPropertyWrapper(mockProperty("test", "<h1>test</h1>"), true);
        assertThat(getStringValues(p).size(), is(1));
        assertThat(getStringValues(p).get(0), is("&lt;h1&gt;test&lt;/h1&gt;"));

        p = new HTMLEscapingPropertyWrapper(mockProperty("test", "<h1>test</h1>", "<h2>test</h2>"), true);
        assertThat(getStringValues(p).size(), is(2));
        // TODO: Fix bug
//        assertThat(getStringValues(p).get(0), is("&lt;h1&gt;test&lt;/h1&gt;"));
//        assertThat(getStringValues(p).get(1), is("&lt;h2&gt;test&lt;/h2&gt;"));
    }

    @Test
    public void getStringValuesTestFromDouble() throws RepositoryException {
        Property p = mockProperty("test", 2.3D);
        assertThat(getStringValues(p).get(0), is("2.3"));
    }

    @Test
    public void getStringValuesTestFromLong() throws RepositoryException {
        Property p = mockProperty("test", 23L);
        assertThat(getStringValues(p).get(0), is("23"));
    }

    @Test
    public void getStringValuesTestFromBoolean() throws RepositoryException {
        Property p = mockProperty("test", true, false);
        assertThat(getStringValues(p).get(0), is("true"));
        assertThat(getStringValues(p).get(1), is("false"));
    }

    @Test
    public void getStringValuesTestFromCalendar() throws RepositoryException {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        Property p = mockProperty("test", cal);
        assertThat(getStringValues(p).get(0), is("1970-01-01T01:00:00.000+01:00"));
    }
}
