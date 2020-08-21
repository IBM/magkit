package com.aperto.magkit.query.sql2.condition;

import info.magnolia.jcr.util.NodeTypes;

import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Builder class for date property conditions.
 *
 * @author wolf.bubenik@aperto.com
 * @since 16.04.2020
 */
public final class Sql2CalendarCondition extends Sql2PropertyCondition<Sql2CalendarCondition, Calendar> {

    private Sql2CalendarCondition(final String property) {
        super(property);
    }

    public static Sql2CompareNot<Calendar> property(final String name) {
        return new Sql2CalendarCondition(name);
    }

    public static Sql2CompareNot<Calendar> created() {
        return property(NodeTypes.Created.CREATED);
    }

    public static Sql2CompareNot<Calendar> lastActivated() {
        return property(NodeTypes.Activatable.LAST_ACTIVATED);
    }

    public static Sql2CompareNot<Calendar> lastModified() {
        return property(NodeTypes.LastModified.LAST_MODIFIED);
    }

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
     * Copied code from org.apache.jackrabbit.util.ISO8601. Modified to use existing StringBuilder.
     *
     * @param cal the date to be converted into ISO8601 string as Calendar
     * @param sql2 the StringBuilder where to add the date string to
     * @throws IllegalArgumentException when one of the parameters is null or the year has more than 4 digits.
     */
    private void appendIso8601(@NotNull Calendar cal, @NotNull StringBuilder sql2) throws IllegalArgumentException {
        /*
         * the format of the date/time string is:
         * YYYY-MM-DDThh:mm:ss.SSSTZD
         *
         * note that we cannot use java.text.SimpleDateFormat for
         * formatting because it can't handle years <= 0 and TZD's
         */

        // year ([-]YYYY)
        appendZeroPaddedInt(sql2, getYear(cal), 4);
        sql2.append('-');
        // month (MM)
        appendZeroPaddedInt(sql2, cal.get(Calendar.MONTH) + 1, 2);
        sql2.append('-');
        // day (DD)
        appendZeroPaddedInt(sql2, cal.get(Calendar.DAY_OF_MONTH), 2);
        sql2.append('T');
        // hour (hh)
        appendZeroPaddedInt(sql2, cal.get(Calendar.HOUR_OF_DAY), 2);
        sql2.append(':');
        // minute (mm)
        appendZeroPaddedInt(sql2, cal.get(Calendar.MINUTE), 2);
        sql2.append(':');
        // second (ss)
        appendZeroPaddedInt(sql2, cal.get(Calendar.SECOND), 2);
        sql2.append('.');
        // millisecond (SSS)
        appendZeroPaddedInt(sql2, cal.get(Calendar.MILLISECOND), 3);
        // time zone designator (Z or +00:00 or -00:00)
        TimeZone tz = cal.getTimeZone();
        // determine offset of timezone from UTC (incl. daylight saving)
        int offset = tz.getOffset(cal.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs((offset / 60000) / 60);
            int minutes = Math.abs((offset / 60000) % 60);
            sql2.append(offset < 0 ? '-' : '+');
            appendZeroPaddedInt(sql2, hours, 2);
            sql2.append(':');
            appendZeroPaddedInt(sql2, minutes, 2);
        } else {
            sql2.append('Z');
        }
    }

    private int getYear(Calendar cal) throws IllegalArgumentException {
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

        if (year > 9999 || year < -9999) {
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
            if (n < Math.pow(10, exp)) {
                buf.append('0');
            } else {
                break;
            }
        }
        buf.append(n);
    }
}
