package com.aperto.magkit.query.sql2.condition;

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

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test Sql2CalendarCondition.
 *
 * @author wolf.bubenik@aperto.com
 * @since (20.04.2020)
 */
public class Sql2CalendarConditionTest {

    private final Calendar _date = Calendar.getInstance();

    @Before
    public void setUp() throws Exception {
        _date.set(2020, Calendar.MAY, 4, 15, 30, 0);
        _date.set(Calendar.MILLISECOND, 0);
    }

    @Test
    public void property() {
        assertThat(Sql2CalendarCondition.property(null).equalsAll().values(_date).asString(), is(""));
        assertThat(Sql2CalendarCondition.property("").equalsAll().values(_date).asString(), is(""));
        assertThat(Sql2CalendarCondition.property("now").equalsAll().values(_date, _date).asString(), is("([now] = cast('2020-05-04T15:30:00.000+02:00' as date) AND [now] = cast('2020-05-04T15:30:00.000+02:00' as date))"));
        assertThat(Sql2CalendarCondition.property("now").equalsAny().values(_date, _date).asString(), is("([now] = cast('2020-05-04T15:30:00.000+02:00' as date) OR [now] = cast('2020-05-04T15:30:00.000+02:00' as date))"));
        assertThat(Sql2CalendarCondition.property("now").excludeAll().values(_date, _date).asString(), is("([now] <> cast('2020-05-04T15:30:00.000+02:00' as date) AND [now] <> cast('2020-05-04T15:30:00.000+02:00' as date))"));
        assertThat(Sql2CalendarCondition.property("now").excludeAny().values(_date, _date).asString(), is("([now] <> cast('2020-05-04T15:30:00.000+02:00' as date) OR [now] <> cast('2020-05-04T15:30:00.000+02:00' as date))"));
        assertThat(Sql2CalendarCondition.property("now").greaterThan().value(_date).asString(), is("[now] > cast('2020-05-04T15:30:00.000+02:00' as date)"));
        assertThat(Sql2CalendarCondition.property("now").greaterOrEqualThan().value(_date).asString(), is("[now] >= cast('2020-05-04T15:30:00.000+02:00' as date)"));
        assertThat(Sql2CalendarCondition.property("now").lowerOrEqualThan().value(_date).asString(), is("[now] <= cast('2020-05-04T15:30:00.000+02:00' as date)"));
        assertThat(Sql2CalendarCondition.property("now").lowerThan().value(_date).asString(), is("[now] < cast('2020-05-04T15:30:00.000+02:00' as date)"));
    }

    @Test
    public void created() {
        assertThat(Sql2CalendarCondition.created().lowerOrEqualThan().value(null).asString(), is(""));
        assertThat(Sql2CalendarCondition.created().lowerOrEqualThan().value(_date).asString(), is("[mgnl:created] <= cast('2020-05-04T15:30:00.000+02:00' as date)"));
    }

    @Test
    public void lastActivated() {
        assertThat(Sql2CalendarCondition.lastActivated().lowerOrEqualThan().value(null).asString(), is(""));
        assertThat(Sql2CalendarCondition.lastActivated().lowerOrEqualThan().value(_date).asString(), is("[mgnl:lastActivated] <= cast('2020-05-04T15:30:00.000+02:00' as date)"));
    }

    @Test
    public void lastModified() {
        assertThat(Sql2CalendarCondition.lastModified().lowerOrEqualThan().value(null).asString(), is(""));
        assertThat(Sql2CalendarCondition.lastModified().lowerOrEqualThan().value(_date).asString(), is("[mgnl:lastModified] <= cast('2020-05-04T15:30:00.000+02:00' as date)"));
    }

    @Test
    public void deleted() {
        assertThat(Sql2CalendarCondition.deleted().lowerOrEqualThan().value(null).asString(), is(""));
        assertThat(Sql2CalendarCondition.deleted().lowerOrEqualThan().value(_date).asString(), is("[mgnl:deleted] <= cast('2020-05-04T15:30:00.000+02:00' as date)"));
    }
}
