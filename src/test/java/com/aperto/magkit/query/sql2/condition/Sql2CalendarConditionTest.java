package com.aperto.magkit.query.sql2.condition;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2CalendarCondition.
 *
 * @author wolf.bubenik@aperto.com
 * @since (20.04.2020)
 */
public class Sql2CalendarConditionTest {

    private Calendar _date = Calendar.getInstance();

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