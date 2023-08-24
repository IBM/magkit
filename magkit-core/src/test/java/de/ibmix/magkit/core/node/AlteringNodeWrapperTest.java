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

import de.ibmix.magkit.core.utils.NodeUtils;
import org.junit.After;
import org.junit.Test;

import javax.jcr.Node;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static de.ibmix.magkit.core.utils.PropertyUtils.getBooleanValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getBooleanValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getCalendarValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getCalendarValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getDoubleValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getDoubleValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getLongValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getLongValues;
import static de.ibmix.magkit.core.utils.PropertyUtils.getStringValue;
import static de.ibmix.magkit.core.utils.PropertyUtils.getStringValues;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeStubbingOperation.stubTemplate;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;

/**
 * Test AlteringNodeWrapper.
 *
 * @author wolf.bubenik
 * @since 10.05.19.
 */
public class AlteringNodeWrapperTest {

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void getNodeToWrap() {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(nodeWrapper.getNodeToWrap(), is(node));
    }

    @Test
    public void withStringProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(getStringValue(nodeWrapper.getProperty("names")), nullValue());

        nodeWrapper.withProperty("names", "value1", "value2");
        assertThat(getStringValue(nodeWrapper.getProperty("names")), is("value1"));
        assertThat(nodeWrapper.getProperty("names").isMultiple(), is(true));

        List<String> stringValues = getStringValues(nodeWrapper, "names");
        assertThat(stringValues.size(), is(2));
        Iterator<String> iterator = stringValues.iterator();
        assertThat(iterator.next(), is("value1"));
        assertThat(iterator.next(), is("value2"));
    }

    @Test
    public void withBooleanProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(getBooleanValue(nodeWrapper.getProperty("test")), nullValue());

        nodeWrapper.withProperty("test", true, false);
        assertThat(getBooleanValue(nodeWrapper.getProperty("test")), is(true));
        assertThat(nodeWrapper.getProperty("test").isMultiple(), is(true));

        List<Boolean> values = getBooleanValues(nodeWrapper.getProperty("test"));
        assertThat(values.size(), is(2));
        Iterator<Boolean> iterator = values.iterator();
        assertThat(iterator.next(), is(true));
        assertThat(iterator.next(), is(false));
    }

    @Test
    public void withLongProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(getLongValue(nodeWrapper.getProperty("test")), nullValue());

        nodeWrapper.withProperty("test", 3L, 2L);
        assertThat(getLongValue(nodeWrapper.getProperty("test")), is(3L));
        assertThat(nodeWrapper.getProperty("test").isMultiple(), is(true));

        List<Long> values = getLongValues(nodeWrapper.getProperty("test"));
        assertThat(values.size(), is(2));
        Iterator<Long> iterator = values.iterator();
        assertThat(iterator.next(), is(3L));
        assertThat(iterator.next(), is(2L));
    }

    @Test
    public void withCalendarProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(getCalendarValue(nodeWrapper.getProperty("test")), nullValue());

        Calendar now = Calendar.getInstance();
        nodeWrapper.withProperty("test", now);
        assertThat(getCalendarValue(nodeWrapper.getProperty("test")), is(now));
        assertThat(getCalendarValues(nodeWrapper.getProperty("test")).size(), is(1));
        assertThat(getCalendarValues(nodeWrapper.getProperty("test")).get(0), is(now));
        assertThat(nodeWrapper.getProperty("test").isMultiple(), is(false));
    }

    @Test
    public void withDoubleProperty() throws Exception {
        Node node = mock(Node.class);
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(getDoubleValue(nodeWrapper.getProperty("test")), nullValue());

        nodeWrapper.withProperty("test", 3.2D, 2.3D);
        assertThat(getDoubleValue(nodeWrapper.getProperty("test")), is(3.2D));
        assertThat(nodeWrapper.getProperty("test").isMultiple(), is(true));

        List<Double> values = getDoubleValues(nodeWrapper.getProperty("test"));
        assertThat(values.size(), is(2));
        Iterator<Double> iterator = values.iterator();
        assertThat(iterator.next(), is(3.2D));
        assertThat(iterator.next(), is(2.3D));
    }

    @Test
    public void withTemplate() throws Exception {
        Node node = mockNode("test", stubTemplate("test-template"));
        AlteringNodeWrapper nodeWrapper = new AlteringNodeWrapper(node);
        assertThat(NodeUtils.getTemplate(nodeWrapper), is("test-template"));

        nodeWrapper.withTemplate("wrapped-template");
        assertThat(NodeUtils.getTemplate(nodeWrapper), is("wrapped-template"));
    }

    @Test
    public void getProperty() throws Exception {
        Node node = mockNode("test");
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(node);
        assertThat(getStringValue(wrapper.getProperty("p0")), nullValue());

        stubProperty("p0", "test-value").of(node);
        assertThat(getStringValue(wrapper.getProperty("p0")), is("test-value"));
        assertThat(wrapper.getProperty("p0").isMultiple(), is(false));

        wrapper.withProperty("p0", "wrapped");
        assertThat(getStringValue(wrapper.getProperty("p0")), is("wrapped"));
        assertThat(wrapper.getProperty("p0").isMultiple(), is(false));

        wrapper.withMappedProperty("p0", "mapped");
        assertThat(getStringValue(wrapper.getProperty("p0")), nullValue());

        wrapper.withProperty("mapped", "mapped");
        assertThat(getStringValue(wrapper.getProperty("p0")), is("mapped"));
        assertThat(wrapper.getProperty("p0").isMultiple(), is(false));
    }

    @Test
    public void hasProperty() throws Exception {
        Node node = mockNode("test");
        AlteringNodeWrapper wrapper = new AlteringNodeWrapper(node);
        assertThat(wrapper.hasProperty("test"), is(false));
        assertThat(wrapper.hasProperty("mapped"), is(false));

        stubProperty("test", "test-value").of(node);
        assertThat(wrapper.hasProperty("test"), is(true));
        assertThat(wrapper.hasProperty("mapped"), is(false));

        wrapper.withMappedProperty("mapped", "test");
        assertThat(wrapper.hasProperty("test"), is(true));
        assertThat(wrapper.hasProperty("mapped"), is(true));
    }
}
