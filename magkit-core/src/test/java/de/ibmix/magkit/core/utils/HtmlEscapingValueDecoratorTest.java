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


import de.ibmix.magkit.test.jcr.ValueMockUtils;
import info.magnolia.jcr.wrapper.HTMLEscapingContentDecorator;
import org.junit.Test;

import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Testing HTMLEscapingContentDecorator.
 *
 * @author wolf.bubenik.ibmix.de
 * @since 2024-01-04
 */
public class HtmlEscapingValueDecoratorTest {

    private HTMLEscapingContentDecorator _decorator = new HTMLEscapingContentDecorator(true);

    @Test
    public void getString() throws RepositoryException {
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue("<h1>test</h1>"), _decorator);
        assertThat(value.getString(), is("&lt;h1&gt;test&lt;/h1&gt;"));
        assertThat(value.getType(), is(PropertyType.STRING));
    }

    @Test
    public void getStream() throws RepositoryException {
        Binary binary = ValueMockUtils.mockBinary("test");
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(binary), _decorator);
        assertThat(value.getStream(), is(binary.getStream()));
        assertThat(value.getType(), is(PropertyType.BINARY));
    }

    @Test
    public void getBinary() throws RepositoryException {
        Binary binary = ValueMockUtils.mockBinary("test");
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(binary), _decorator);
        assertThat(value.getBinary(), is(binary));
        assertThat(value.getType(), is(PropertyType.BINARY));
    }

    @Test
    public void getLong() throws RepositoryException {
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(123L), _decorator);
        assertThat(value.getLong(), is(123L));
        assertThat(value.getType(), is(PropertyType.LONG));
    }

    @Test
    public void getDouble() throws RepositoryException {
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(0.123D), _decorator);
        assertThat(value.getDouble(), is(0.123D));
        assertThat(value.getType(), is(PropertyType.DOUBLE));
    }

    @Test
    public void getDecimal() throws RepositoryException {
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(456L), _decorator);
        assertThat(value.getDecimal().longValue(), is(456L));
        assertThat(value.getType(), is(PropertyType.LONG));
    }

    @Test
    public void getDate() throws RepositoryException {
        Calendar date = Calendar.getInstance();
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(date), _decorator);
        assertThat(value.getDate(), is(date));
        assertThat(value.getType(), is(PropertyType.DATE));
    }

    @Test
    public void getBoolean() throws RepositoryException {
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(true), _decorator);
        assertThat(value.getBoolean(), is(true));
        assertThat(value.getType(), is(PropertyType.BOOLEAN));
    }
}