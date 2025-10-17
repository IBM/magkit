package de.ibmix.magkit.query.sql2.condition;

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

import info.magnolia.jcr.util.NodeTypes;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Builder for date (Calendar) property conditions supporting equality and range related comparisons
 * on arbitrary date properties as well as common Magnolia audit properties (created, last activated, etc.).
 * <p>
 * Values are rendered as ISO 8601 using a custom in-place formatter copied (and slightly adapted) from
 * {@code org.apache.jackrabbit.util.ISO8601} to avoid intermediate object creation.
 * </p>
 * Thread-safety: Not thread safe.
 * Null handling: Methods silently ignore {@code null} values and produce no constraint output.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-16
 */
public final class Sql2CalendarCondition extends Sql2PropertyCondition<Sql2CalendarCondition, Calendar> {

    private static final int TEN = 10;
    private static final int DIGITS_TEN = 2;
    private static final int DIGITS_HUNDRED = 3;
    private static final int DIGITS_THOUSAND = 4;
    private static final int MINUTES_HOUR = 60;
    private static final int MILLISECONDS_MINUTE = 60000;
    private static final int MAX_YEAR = 9999;
    private static final int MIN_YEAR = -9999;
    private Sql2CalendarCondition(final String property) {
        super(property);
    }

    /**
     * Start a date condition on an arbitrary property.
     *
     * @param name property name
     * @return comparison API
     */
    public static Sql2CompareNot<Calendar> property(final String name) {
        return new Sql2CalendarCondition(name);
    }

    /**
     * Start a condition on the Magnolia created date property.
     *
     * @return comparison API
     */
    public static Sql2CompareNot<Calendar> created() {
        return property(NodeTypes.Created.CREATED);
    }

    /**
     * Start a condition on the Magnolia last activated property.
     *
     * @return comparison API
     */
    public static Sql2CompareNot<Calendar> lastActivated() {
        return property(NodeTypes.Activatable.LAST_ACTIVATED);
    }

    /**
     * Start a condition on the Magnolia last modified property.
     *
     * @return comparison API
     */
    public static Sql2CompareNot<Calendar> lastModified() {
        return property(NodeTypes.LastModified.LAST_MODIFIED);
    }

    /**
     * Start a condition on the Magnolia deleted property.
     *
     * @return comparison API
     */
    public static Sql2CompareNot<Calendar> deleted() {
        return property(NodeTypes.Deleted.DELETED);
    }

    @Override
    Sql2CalendarCondition me() {
        return this;
    }

    @Override
    void appendValueConstraint(StringBuilder sql2, String selectorName, String name, Calendar value) {
        if (value != null) {
            if (isNotBlank(selectorName)) {
                sql2.append(selectorName).append('.');
            }
            sql2.append('[').append(name).append(']').append(getCompareOperator()).append("cast('");
            appendIso8601(value, sql2);
            sql2.append("' as date)");
        }
    }

    /**
     * Convert the calendar to ISO8601 and append to buffer.
     *
     * @param cal calendar to format
     * @param sql2 destination buffer
     * @throws IllegalArgumentException if year out of supported range
     */
    private void appendIso8601(final Calendar cal, final StringBuilder sql2) {
        /*
         * the format of the date/time string is:
         * YYYY-MM-DDThh:mm:ss.SSSTZD
         *
         * note that we cannot use java.text.SimpleDateFormat for
         * formatting because it can't handle years <= 0 and TZD's
         */

        // year ([-]YYYY)
        appendZeroPaddedInt(sql2, getYear(cal), DIGITS_THOUSAND);
        sql2.append('-');
        // month (MM)
        appendZeroPaddedInt(sql2, cal.get(Calendar.MONTH) + 1, DIGITS_TEN);
        sql2.append('-');
        // day (DD)
        appendZeroPaddedInt(sql2, cal.get(Calendar.DAY_OF_MONTH), DIGITS_TEN);
        sql2.append('T');
        // hour (hh)
        appendZeroPaddedInt(sql2, cal.get(Calendar.HOUR_OF_DAY), DIGITS_TEN);
        sql2.append(':');
        // minute (mm)
        appendZeroPaddedInt(sql2, cal.get(Calendar.MINUTE), DIGITS_TEN);
        sql2.append(':');
        // second (ss)
        appendZeroPaddedInt(sql2, cal.get(Calendar.SECOND), DIGITS_TEN);
        sql2.append('.');
        // millisecond (SSS)
        appendZeroPaddedInt(sql2, cal.get(Calendar.MILLISECOND), DIGITS_HUNDRED);
        // time zone designator (Z or +00:00 or -00:00)
        TimeZone tz = cal.getTimeZone();
        // determine offset of timezone from UTC (incl. daylight saving)
        int offset = tz.getOffset(cal.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs((offset / MILLISECONDS_MINUTE) / MINUTES_HOUR);
            int minutes = Math.abs((offset / MILLISECONDS_MINUTE) % MINUTES_HOUR);
            sql2.append(offset < 0 ? '-' : '+');
            appendZeroPaddedInt(sql2, hours, DIGITS_TEN);
            sql2.append(':');
            appendZeroPaddedInt(sql2, minutes, DIGITS_TEN);
        } else {
            sql2.append('Z');
        }
    }

    private int getYear(Calendar cal) {
        // determine era and adjust year if necessary
        int year = cal.get(Calendar.YEAR);
        if (cal.isSet(Calendar.ERA) && cal.get(Calendar.ERA) == GregorianCalendar.BC) {
            /*
             * calculate year using astronomical system:
             * year n BCE => astronomical year -n + 1
             * 0 - year + 1 = 1 - year
             */
            year = 1 - year;
        }

        if (year > MAX_YEAR || year < MIN_YEAR) {
            throw new IllegalArgumentException("Calendar has more than four year digits, cannot be formatted as ISO8601: " + year);
        }
        return year;
    }

    private void appendZeroPaddedInt(StringBuilder buf, int number, int precision) {
        int n = number;
        if (n < 0) {
            buf.append('-');
            n = -n;
        }

        for (int exp = precision - 1; exp > 0; exp--) {
            if (n < Math.pow(TEN, exp)) {
                buf.append('0');
            } else {
                break;
            }
        }
        buf.append(n);
    }
}
