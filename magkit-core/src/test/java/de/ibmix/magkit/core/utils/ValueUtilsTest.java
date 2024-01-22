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
import org.junit.After;
import org.junit.Test;

import javax.jcr.Binary;
import java.math.BigDecimal;
import java.util.Calendar;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.jcr.ValueMockUtils.mockBinary;
import static de.ibmix.magkit.test.jcr.ValueMockUtils.mockValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Test ValueUtils.
 *
 * @author wolf.bubenik
 * @since 21.12.18.
 */
public class ValueUtilsTest {

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void valueToString() throws Exception {
        assertThat(ValueUtils.valueToString(null), nullValue());
        assertThat(ValueUtils.valueToString(mockValue((String) null)), nullValue());
        assertThat(ValueUtils.valueToString(mockValue("")), is(""));
        assertThat(ValueUtils.valueToString(mockValue("test")), is("test"));
        assertThat(ValueUtils.valueToString(mockValue(true)), is("true"));
        assertThat(ValueUtils.valueToString(mockValue(12L)), is("12"));
        assertThat(ValueUtils.valueToString(mockValue(23.5D)), is("23.5"));
        assertThat(ValueUtils.valueToString(mockValue(mockBinary("test"))), is("test"));
    }

    @Test
    public void valueToString1() throws Exception {
        assertThat(ValueUtils.valueToString(null, "test"), is("test"));
    }

    @Test
    public void valueToCalendar() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0L);
        assertThat(ValueUtils.valueToCalendar(null), nullValue());
        assertThat(ValueUtils.valueToCalendar(mockValue((Calendar) null)), nullValue());
        assertThat(ValueUtils.valueToCalendar(mockValue("")), nullValue());
        assertThat(ValueUtils.valueToCalendar(mockValue("test")), nullValue());
        assertThat(ValueUtils.valueToCalendar(mockValue("1970-01-01T01:00:00.000+01:00")).getTimeInMillis(), is(0L));
        assertThat(ValueUtils.valueToCalendar(mockValue(true)), nullValue());
        assertThat(ValueUtils.valueToCalendar(mockValue("0")), is(cal));
        assertThat(ValueUtils.valueToCalendar(mockValue(0L)), is(cal));
        assertThat(ValueUtils.valueToCalendar(mockValue(0.0D)), is(cal));
        assertThat(ValueUtils.valueToCalendar(mockValue(mockBinary("0"))), nullValue());
    }

    @Test
    public void valueToCalendar1() throws Exception {
        Calendar cal = Calendar.getInstance();
        assertThat(ValueUtils.valueToCalendar(null, cal), is(cal));
    }

    @Test
    public void valueToLong() throws Exception {
        Calendar cal = Calendar.getInstance();
        assertThat(ValueUtils.valueToLong(null), nullValue());
        assertThat(ValueUtils.valueToLong(mockValue("")), nullValue());
        assertThat(ValueUtils.valueToLong(mockValue(cal)), is(cal.getTimeInMillis()));
        assertThat(ValueUtils.valueToLong(mockValue("test")), nullValue());
        assertThat(ValueUtils.valueToLong(mockValue("1970-01-01T01:00:00.000+01:00")), nullValue());
        assertThat(ValueUtils.valueToLong(mockValue("23")), is(23L));
        assertThat(ValueUtils.valueToLong(mockValue("23.42")), is(23L));
        assertThat(ValueUtils.valueToLong(mockValue(true)), nullValue());
        assertThat(ValueUtils.valueToLong(mockValue(false)), nullValue());
        assertThat(ValueUtils.valueToLong(mockValue(0L)), is(0L));
        assertThat(ValueUtils.valueToLong(mockValue(0.0D)), is(0L));
        assertThat(ValueUtils.valueToLong(mockValue(mockBinary("0123"))), nullValue());
    }

    @Test
    public void valueToLong1() throws Exception {
        assertThat(ValueUtils.valueToLong(null, 42L), is(42L));
    }

    @Test
    public void valueToDouble() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0L);
        assertThat(ValueUtils.valueToDouble(null), nullValue());
        assertThat(ValueUtils.valueToDouble(mockValue("")), nullValue());
        assertThat(ValueUtils.valueToDouble(mockValue(cal)), is(0.0D));
        assertThat(ValueUtils.valueToDouble(mockValue("test")), nullValue());
        assertThat(ValueUtils.valueToDouble(mockValue("1970-01-01T01:00:00.000+01:00")), nullValue());
        assertThat(ValueUtils.valueToDouble(mockValue("23")), is(23D));
        assertThat(ValueUtils.valueToDouble(mockValue("23.42")), is(23.42D));
        assertThat(ValueUtils.valueToDouble(mockValue(true)), nullValue());
        assertThat(ValueUtils.valueToDouble(mockValue(false)), nullValue());
        assertThat(ValueUtils.valueToDouble(mockValue(23L)), is(23D));
        assertThat(ValueUtils.valueToDouble(mockValue(23.420D)), is(23.42D));
        assertThat(ValueUtils.valueToDouble(mockValue(mockBinary("0123"))), nullValue());
    }

    @Test
    public void valueToDouble1() {
        assertThat(ValueUtils.valueToDouble(null, 5.5D), is(5.5D));
    }

    @Test
    public void valueToBoolean() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0L);
        assertThat(ValueUtils.valueToBoolean(null), nullValue());
        assertThat(ValueUtils.valueToBoolean(mockValue("")), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue(cal)), nullValue());
        assertThat(ValueUtils.valueToBoolean(mockValue("test")), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue("1970-01-01T01:00:00.000+01:00")), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue("0")), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue("true")), is(true));
        assertThat(ValueUtils.valueToBoolean(mockValue("false")), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue(true)), is(true));
        assertThat(ValueUtils.valueToBoolean(mockValue(false)), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue(0L)), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue(1L)), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue(23L)), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue(0.0D)), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue(23.420D)), is(false));
        assertThat(ValueUtils.valueToBoolean(mockValue(mockBinary("0123"))), nullValue());
    }

    @Test
    public void valueToBoolean1() {
        assertThat(ValueUtils.valueToBoolean(null, true), is(true));
    }

    @Test
    public void valueToBinary() throws Exception {
        Binary a = mockBinary("fallback");
        Binary b = mockBinary("test");
        assertThat(ValueUtils.valueToBinary(null, a), is(a));
        assertThat(ValueUtils.valueToBinary(mockValue(b), a), is(b));
    }

    @Test
    public void valueToBinary1() throws Exception {
        assertThat(ValueUtils.valueToBinary(null), nullValue());
        assertThat(ValueUtils.valueToBinary(mockValue((Binary) null)), nullValue());

        Binary b = mockBinary("test");
        assertThat(ValueUtils.valueToBinary(mockValue(b)), is(b));
    }

    @Test
    public void valueToBigDecimal() throws Exception {
        assertThat(ValueUtils.valueToBigDecimal(null, BigDecimal.TEN), is(BigDecimal.TEN));
        assertThat(ValueUtils.valueToBigDecimal(mockValue(0L), BigDecimal.TEN), is(BigDecimal.ZERO));
    }

}
