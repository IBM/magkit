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
}
