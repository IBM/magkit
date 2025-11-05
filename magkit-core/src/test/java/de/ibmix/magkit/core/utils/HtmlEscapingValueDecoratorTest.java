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
import org.junit.jupiter.api.Test;

import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals("&lt;h1&gt;test&lt;/h1&gt;", value.getString());
        assertEquals(PropertyType.STRING, value.getType());
    }

    @Test
    public void getStream() throws RepositoryException {
        Binary binary = ValueMockUtils.mockBinary("test");
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(binary), _decorator);
        assertEquals(binary.getStream(), value.getStream());
        assertEquals(PropertyType.BINARY, value.getType());
    }

    @Test
    public void getBinary() throws RepositoryException {
        Binary binary = ValueMockUtils.mockBinary("test");
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(binary), _decorator);
        assertEquals(binary, value.getBinary());
        assertEquals(PropertyType.BINARY, value.getType());
    }

    @Test
    public void getLong() throws RepositoryException {
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(123L), _decorator);
        assertEquals(123L, value.getLong());
        assertEquals(PropertyType.LONG, value.getType());
    }

    @Test
    public void getDouble() throws RepositoryException {
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(0.123D), _decorator);
        assertEquals(0.123D, value.getDouble());
        assertEquals(PropertyType.DOUBLE, value.getType());
    }

    @Test
    public void getDecimal() throws RepositoryException {
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(456L), _decorator);
        assertEquals(456L, value.getDecimal().longValue());
        assertEquals(PropertyType.LONG, value.getType());
    }

    @Test
    public void getDate() throws RepositoryException {
        Calendar date = Calendar.getInstance();
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(date), _decorator);
        assertEquals(date, value.getDate());
        assertEquals(PropertyType.DATE, value.getType());
    }

    @Test
    public void getBoolean() throws RepositoryException {
        Value value = new HtmlEscapingValueDecorator(ValueMockUtils.mockValue(true), _decorator);
        assertTrue(value.getBoolean());
        assertEquals(PropertyType.BOOLEAN, value.getType());
    }
}