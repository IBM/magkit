package com.aperto.magkit.utils;

import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import java.util.Collection;

import static com.aperto.magkit.mockito.NodeMockUtils.mockNode;
import static com.aperto.magkit.mockito.NodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.NodeStubbingOperation.stubProperty;
import static com.aperto.magkit.utils.PropertyUtils.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link PropertyUtils}.
 *
 * @author frank.sommer
 * @since 09.10.12
 */
public class PropertyUtilsTest {

    @Test
    public void testNullValue() throws Exception {
        Collection<Property> properties = retrieveMultiSelectProperties(null);
        assertThat(properties, nullValue());
    }

    @Test
    public void testRetrieveOrderedMultiSelectValues() throws Exception {
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
    public void testGetValidLong() {
        Node node = mockNode("/node", stubProperty("test", 12L));
        Long longValue = getLong(node, "test", 0L);
        assertThat(longValue, is(12L));
    }

    @Test
    public void testGetInValidLong() {
        Node node = mockNode("/node", stubProperty("test", "abc"));
        Long longValue = getLong(node, "test", 0L);
        assertThat(longValue, is(0L));
    }
}
