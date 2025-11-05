package de.ibmix.magkit.core.utils;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2025 IBM iX
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
import org.junit.jupiter.api.Test;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static de.ibmix.magkit.test.jcr.ValueMockUtils.mockValue;
import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link ValueUtils} covering success, null and exception fallback scenarios for every conversion method.
 * Ensures consistent null handling and fallback behavior across all supported target types.
 *
 * Test strategy:
 * - Success conversions: All typed getters return concrete values and are passed through unchanged (ignoring fallbacks).
 * - Null input: Methods return provided fallback or null when value parameter is null.
 * - Exception path: A throwing {@link Value} implementation raises a {@link RepositoryException}; methods return fallback.
 *
 * @author GitHub Copilot, supplemented by wolf.bubenik
 * @since 2025-10-20
 */
public class ValueUtilsTest {

    /**
     * Verifies valueToString returns null when value is null and no fallback given.
     */
    @Test
    public void valueToString() throws RepositoryException {
        assertNull(ValueUtils.valueToString(null));
        assertEquals("fb", ValueUtils.valueToString(null, "fb"));

        Value v = mockValue("string");
        assertEquals("string", ValueUtils.valueToString(v, "fb"));

        doThrow(new RepositoryException("fail")).when(v).getString();
        assertEquals("fb", ValueUtils.valueToString(v, "fb"));
        assertNull(ValueUtils.valueToString(v));
    }

    /**
     * Verifies calendar conversion success and fallback behavior.
     */
    @Test
    public void valueToCalendar() throws RepositoryException {
        assertNull(ValueUtils.valueToCalendar(null));

        Calendar fb = new GregorianCalendar(2024, Calendar.DECEMBER, 31);
        assertEquals(fb, ValueUtils.valueToCalendar(null, fb));

        Calendar cal = new GregorianCalendar(2025, Calendar.JANUARY, 1);
        Value v = mockValue(cal);
        assertEquals(cal, ValueUtils.valueToCalendar(v));

        doThrow(new RepositoryException("fail")).when(v).getDate();
        assertEquals(fb, ValueUtils.valueToCalendar(v, fb));

        assertNull(ValueUtils.valueToCalendar(v));
    }

    /**
     * Verifies long conversion success and exception fallback.
     */
    @Test
    public void valueToLong() throws RepositoryException {
        assertNull(ValueUtils.valueToLong(null));
        assertEquals(7L, ValueUtils.valueToLong(null, 7L));

        Value v = mockValue(42L);
        assertEquals(42L, ValueUtils.valueToLong(v));
        assertEquals(42L, ValueUtils.valueToLong(v, 9L));

        doThrow(new RepositoryException("fail")).when(v).getLong();
        assertEquals(9L, ValueUtils.valueToLong(v, 9L));
        assertNull(ValueUtils.valueToLong(v));
    }

    /**
     * Verifies double conversion success and exception fallback.
     */
    @Test
    public void valueToDouble() throws RepositoryException {
        assertNull(ValueUtils.valueToDouble(null));
        assertEquals(1.1d, ValueUtils.valueToDouble(null, 1.1d));

        Value v = mockValue(3.5d);
        assertEquals(3.5d, ValueUtils.valueToDouble(v));
        assertEquals(3.5d, ValueUtils.valueToDouble(v, 1.1d));

        doThrow(new RepositoryException("fail")).when(v).getDouble();
        assertEquals(2.2d, ValueUtils.valueToDouble(v, 2.2d));
        assertNull(ValueUtils.valueToDouble(v));
    }

    /**
     * Verifies boolean conversion success and exception fallback.
     */
    @Test
    public void valueToBoolean() throws RepositoryException {
        assertNull(ValueUtils.valueToBoolean(null));
        assertFalse(ValueUtils.valueToBoolean(null, Boolean.FALSE));

        Value v = mockValue(true);
        assertTrue(ValueUtils.valueToBoolean(v));
        assertTrue(ValueUtils.valueToBoolean(v, false));

        doThrow(new RepositoryException("fail")).when(v).getBoolean();
        assertTrue(ValueUtils.valueToBoolean(v, Boolean.TRUE));
        assertNull(ValueUtils.valueToBoolean(v));
    }

    /**
     * Verifies binary conversion success and exception fallback.
     */
    @Test
    public void valueToBinary() throws RepositoryException {
        Binary fb = ValueMockUtils.mockBinary("fallback binary");
        assertNull(ValueUtils.valueToBinary(null));
        assertEquals(fb, ValueUtils.valueToBinary(null, fb));

        Binary bin = ValueMockUtils.mockBinary("binary");
        Value v = mockValue(bin);
        assertEquals(bin, ValueUtils.valueToBinary(v));
        assertEquals(bin, ValueUtils.valueToBinary(v, fb));

        doThrow(new RepositoryException("fail")).when(v).getBinary();
        assertEquals(fb, ValueUtils.valueToBinary(v, fb));
        assertNull(ValueUtils.valueToBinary(v));
    }

    /**
     * Verifies BigDecimal conversion success and exception fallback.
     */
    @Test
    public void valueToDecimal() throws RepositoryException {
        assertNull(ValueUtils.valueToBigDecimal(null));
        assertEquals(BigDecimal.TEN, ValueUtils.valueToBigDecimal(null, BigDecimal.TEN));

        Value v = mockValue("12.34");
        assertEquals(12.34d, ValueUtils.valueToBigDecimal(v).doubleValue());
        assertEquals(12.34d, ValueUtils.valueToBigDecimal(v, BigDecimal.TEN).doubleValue());

        doThrow(new RepositoryException("fail")).when(v).getDecimal();
        assertEquals(BigDecimal.TEN, ValueUtils.valueToBigDecimal(v, BigDecimal.TEN));
        assertNull(ValueUtils.valueToBigDecimal(v));
    }
}
