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
import org.junit.Test;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static de.ibmix.magkit.test.jcr.ValueMockUtils.mockValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.doThrow;

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
        assertThat(ValueUtils.valueToString(null), nullValue());
        assertThat(ValueUtils.valueToString(null, "fb"), is("fb"));

        Value v = mockValue("string");
        assertThat(ValueUtils.valueToString(v, "fb"), is("string"));

        doThrow(new RepositoryException("fail")).when(v).getString();
        assertThat(ValueUtils.valueToString(v, "fb"), is("fb"));
        assertThat(ValueUtils.valueToString(v), nullValue());
    }

    /**
     * Verifies calendar conversion success and fallback behavior.
     */
    @Test
    public void valueToCalendar() throws RepositoryException {
        assertThat(ValueUtils.valueToCalendar(null), nullValue());

        Calendar fb = new GregorianCalendar(2024, Calendar.DECEMBER, 31);
        assertThat(ValueUtils.valueToCalendar(null, fb), is(fb));

        Calendar cal = new GregorianCalendar(2025, Calendar.JANUARY, 1);
        Value v = mockValue(cal);
        assertThat(ValueUtils.valueToCalendar(v), is(cal));

        doThrow(new RepositoryException("fail")).when(v).getDate();
        assertThat(ValueUtils.valueToCalendar(v, fb), is(fb));

        assertThat(ValueUtils.valueToCalendar(v), nullValue());
    }

    /**
     * Verifies long conversion success and exception fallback.
     */
    @Test
    public void valueToLong() throws RepositoryException {
        assertThat(ValueUtils.valueToLong(null), nullValue());
        assertThat(ValueUtils.valueToLong(null, 7L), is(7L));

        Value v = mockValue(42L);
        assertThat(ValueUtils.valueToLong(v), is(42L));
        assertThat(ValueUtils.valueToLong(v, 9L), is(42L));

        doThrow(new RepositoryException("fail")).when(v).getLong();
        assertThat(ValueUtils.valueToLong(v, 9L), is(9L));
        assertThat(ValueUtils.valueToLong(v), nullValue());
    }

    /**
     * Verifies double conversion success and exception fallback.
     */
    @Test
    public void valueToDouble() throws RepositoryException {
        assertThat(ValueUtils.valueToDouble(null), nullValue());
        assertThat(ValueUtils.valueToDouble(null, 1.1d), is(1.1d));

        Value v = mockValue(3.5d);
        assertThat(ValueUtils.valueToDouble(v), is(3.5d));
        assertThat(ValueUtils.valueToDouble(v, 1.1d), is(3.5d));

        doThrow(new RepositoryException("fail")).when(v).getDouble();
        assertThat(ValueUtils.valueToDouble(v, 2.2d), is(2.2d));
        assertThat(ValueUtils.valueToDouble(v), nullValue());
    }

    /**
     * Verifies boolean conversion success and exception fallback.
     */
    @Test
    public void valueToBoolean() throws RepositoryException {
        assertThat(ValueUtils.valueToBoolean(null), nullValue());
        assertThat(ValueUtils.valueToBoolean(null, Boolean.FALSE), is(false));

        Value v = mockValue(true);
        assertThat(ValueUtils.valueToBoolean(v), is(true));
        assertThat(ValueUtils.valueToBoolean(v, false), is(true));

        doThrow(new RepositoryException("fail")).when(v).getBoolean();
        assertThat(ValueUtils.valueToBoolean(v, Boolean.TRUE), is(true));
        assertThat(ValueUtils.valueToBoolean(v), nullValue());
    }

    /**
     * Verifies binary conversion success and exception fallback.
     */
    @Test
    public void valueToBinary() throws RepositoryException {
        Binary fb = ValueMockUtils.mockBinary("fallback binary");
        assertThat(ValueUtils.valueToBinary(null), nullValue());
        assertThat(ValueUtils.valueToBinary(null, fb), is(fb));

        Binary bin = ValueMockUtils.mockBinary("binary");
        Value v = mockValue(bin);
        assertThat(ValueUtils.valueToBinary(v), is(bin));
        assertThat(ValueUtils.valueToBinary(v, fb), is(bin));

        doThrow(new RepositoryException("fail")).when(v).getBinary();
        assertThat(ValueUtils.valueToBinary(v, fb), is(fb));
        assertThat(ValueUtils.valueToBinary(v), nullValue());
    }

    /**
     * Verifies BigDecimal conversion success and exception fallback.
     */
    @Test
    public void valueToBigDecimal() throws RepositoryException {
        BigDecimal fb = new BigDecimal("9.99");
        assertThat(ValueUtils.valueToBigDecimal(null), nullValue());
        assertThat(ValueUtils.valueToBigDecimal(null, fb), is(fb));

        BigDecimal dec = new BigDecimal("12.34");
        Value v = mockValue(12.34d);
        assertThat(ValueUtils.valueToBigDecimal(v).doubleValue(), is(12.34d));
        assertThat(ValueUtils.valueToBigDecimal(v, fb).doubleValue(), is(12.34d));

        doThrow(new RepositoryException("fail")).when(v).getDecimal();
        assertThat(ValueUtils.valueToBigDecimal(v, fb), is(fb));
        assertThat(ValueUtils.valueToBigDecimal(v), nullValue());
    }
}
