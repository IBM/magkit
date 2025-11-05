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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Sql2CalendarCondition.
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-20
 */
public class Sql2CalendarConditionTest {

    private final Calendar _date = Calendar.getInstance();

    @BeforeEach
    public void setUp() {
        _date.set(2020, Calendar.MAY, 4, 15, 30, 0);
        _date.set(Calendar.MILLISECOND, 0);
        _date.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
    }

    @Test
    public void property() {
        assertEquals("", Sql2CalendarCondition.property(null).equalsAll().values(_date).asString());
        assertEquals("", Sql2CalendarCondition.property("").equalsAll().values(_date).asString());
        assertEquals("([now] = cast('2020-05-04T15:30:00.000+02:00' as date) AND [now] = cast('2020-05-04T15:30:00.000+02:00' as date))", Sql2CalendarCondition.property("now").equalsAll().values(_date, _date).asString());
        assertEquals("([now] = cast('2020-05-04T15:30:00.000+02:00' as date) OR [now] = cast('2020-05-04T15:30:00.000+02:00' as date))", Sql2CalendarCondition.property("now").equalsAny().values(_date, _date).asString());
        assertEquals("([now] <> cast('2020-05-04T15:30:00.000+02:00' as date) AND [now] <> cast('2020-05-04T15:30:00.000+02:00' as date))", Sql2CalendarCondition.property("now").excludeAll().values(_date, _date).asString());
        assertEquals("([now] <> cast('2020-05-04T15:30:00.000+02:00' as date) OR [now] <> cast('2020-05-04T15:30:00.000+02:00' as date))", Sql2CalendarCondition.property("now").excludeAny().values(_date, _date).asString());
        assertEquals("[now] > cast('2020-05-04T15:30:00.000+02:00' as date)", Sql2CalendarCondition.property("now").greaterThan().value(_date).asString());
        assertEquals("[now] >= cast('2020-05-04T15:30:00.000+02:00' as date)", Sql2CalendarCondition.property("now").greaterOrEqualThan().value(_date).asString());
        assertEquals("[now] <= cast('2020-05-04T15:30:00.000+02:00' as date)", Sql2CalendarCondition.property("now").lowerOrEqualThan().value(_date).asString());
        assertEquals("[now] < cast('2020-05-04T15:30:00.000+02:00' as date)", Sql2CalendarCondition.property("now").lowerThan().value(_date).asString());
    }

    @Test
    public void created() {
        assertEquals("", Sql2CalendarCondition.created().lowerOrEqualThan().value(null).asString());
        assertEquals("[mgnl:created] <= cast('2020-05-04T15:30:00.000+02:00' as date)", Sql2CalendarCondition.created().lowerOrEqualThan().value(_date).asString());
    }

    @Test
    public void lastActivated() {
        assertEquals("", Sql2CalendarCondition.lastActivated().lowerOrEqualThan().value(null).asString());
        assertEquals("[mgnl:lastActivated] <= cast('2020-05-04T15:30:00.000+02:00' as date)", Sql2CalendarCondition.lastActivated().lowerOrEqualThan().value(_date).asString());
    }

    @Test
    public void lastModified() {
        assertEquals("", Sql2CalendarCondition.lastModified().lowerOrEqualThan().value(null).asString());
        assertEquals("[mgnl:lastModified] <= cast('2020-05-04T15:30:00.000+02:00' as date)", Sql2CalendarCondition.lastModified().lowerOrEqualThan().value(_date).asString());
    }

    @Test
    public void deleted() {
        assertEquals("", Sql2CalendarCondition.deleted().lowerOrEqualThan().value(null).asString());
        assertEquals("[mgnl:deleted] <= cast('2020-05-04T15:30:00.000+02:00' as date)", Sql2CalendarCondition.deleted().lowerOrEqualThan().value(_date).asString());
    }

    @Test
    public void appendValueConstraintFormatting() {
        StringBuilder result = new StringBuilder();
        Sql2CalendarCondition condition = (Sql2CalendarCondition) Sql2CalendarCondition.property("test").greaterThan();
        condition.appendValueConstraint(result, null, null, null);
        assertEquals("", result.toString());
        result.setLength(0);
        Calendar cal = copy(_date);
        condition.appendValueConstraint(result, null, "test", cal);
        assertEquals("[test] > cast('2020-05-04T15:30:00.000+02:00' as date)", result.toString());
        result.setLength(0);
        condition.appendValueConstraint(result, "", "test", cal);
        assertEquals("[test] > cast('2020-05-04T15:30:00.000+02:00' as date)", result.toString());
        result.setLength(0);
        condition.appendValueConstraint(result, " \t ", "test", cal);
        assertEquals("[test] > cast('2020-05-04T15:30:00.000+02:00' as date)", result.toString());
        result.setLength(0);
        condition.appendValueConstraint(result, "selector", "test", cal);
        assertEquals("selector.[test] > cast('2020-05-04T15:30:00.000+02:00' as date)", result.toString());
    }

    @Test
    public void notCondition() {
        assertEquals("not([test] > cast('2020-05-04T15:30:00.000+02:00' as date))", Sql2CalendarCondition.property("test").not().greaterThan().value(_date).asString());
    }

    @Test
    public void utcTimezoneFormatting() {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.set(2020, Calendar.JUNE, 10, 12, 5, 6);
        utc.set(Calendar.MILLISECOND, 0);
        assertEquals("[date] = cast('2020-06-10T12:05:06.000Z' as date)", Sql2CalendarCondition.property("date").equalsAny().values(utc).asString());
    }

    @Test
    public void negativeTimezoneFormattingAndMillisecondPadding() {
        Calendar minus = Calendar.getInstance(TimeZone.getTimeZone("GMT-10:00"));
        minus.set(2020, Calendar.JANUARY, 2, 3, 4, 5);
        minus.set(Calendar.MILLISECOND, 7);
        assertEquals("[date] = cast('2020-01-02T03:04:05.007-10:00' as date)", Sql2CalendarCondition.property("date").equalsAny().values(minus).asString());
    }

    @Test
    public void bcYearFormatting() {
        GregorianCalendar bc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        bc.clear();
        bc.set(Calendar.ERA, GregorianCalendar.BC);
        bc.set(Calendar.YEAR, 10);
        bc.set(Calendar.MONTH, Calendar.JANUARY);
        bc.set(Calendar.DAY_OF_MONTH, 1);
        bc.set(Calendar.HOUR_OF_DAY, 0);
        bc.set(Calendar.MINUTE, 0);
        bc.set(Calendar.SECOND, 0);
        bc.set(Calendar.MILLISECOND, 0);
        assertEquals("[date] = cast('-0009-01-01T00:00:00.000Z' as date)", Sql2CalendarCondition.property("date").equalsAny().values(bc).asString());
    }

    @Test
    public void yearOutOfRangeThrows() {
        Calendar tooBig = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        tooBig.set(10000, Calendar.JANUARY, 1, 0, 0, 0);
        tooBig.set(Calendar.MILLISECOND, 0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Sql2CalendarCondition.property("date").greaterThan().value(tooBig).asString());
        assertTrue(ex.getMessage().contains("Calendar has more than four year digits"));
    }

    private Calendar copy(Calendar source) {
        Calendar result = Calendar.getInstance(source.getTimeZone());
        result.setTimeInMillis(source.getTimeInMillis());
        return result;
    }
}
