package com.aperto.magkit.utils;

import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import java.util.Collection;

import static com.aperto.magkit.mockito.NodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.NodeStubbingOperation.stubPath;
import static com.aperto.magkit.mockito.NodeStubbingOperation.stubProperty;
import static com.aperto.magkit.utils.PropertyUtils.retrieveMultiSelectProperties;
import static com.aperto.magkit.utils.PropertyUtils.retrieveOrderedMultiSelectValues;
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
        Node node = mockPageNode(
            stubPath("/node/subNode"),
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
}
