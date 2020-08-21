package com.aperto.magkit.query.sql2.condition;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2LongCondition.
 *
 * @author wolf.bubenik@aperto.com
 * @since (24.04.2020)
 */
public class Sql2LongConditionTest {

    @Test
    public void property() {
        assertThat(Sql2LongCondition.property(null).equalsAll().values().asString(), is(""));
        assertThat(Sql2LongCondition.property(null).equalsAll().values(123L).asString(), is(""));
        assertThat(Sql2LongCondition.property("").equalsAll().values(123L).asString(), is(""));
        assertThat(Sql2LongCondition.property("test").equalsAll().values(123L, 234L).asString(), is("([test] = 123 AND [test] = 234)"));
        assertThat(Sql2LongCondition.property("test").equalsAny().values(123L, 234L).asString(), is("([test] = 123 OR [test] = 234)"));
        assertThat(Sql2LongCondition.property("test").excludeAll().values(123L, 234L).asString(), is("([test] <> 123 AND [test] <> 234)"));
        assertThat(Sql2LongCondition.property("test").excludeAny().values(123L, 234L).asString(), is("([test] <> 123 OR [test] <> 234)"));
        assertThat(Sql2LongCondition.property("test").lowerThan().value(123L).asString(), is("[test] < 123"));
        assertThat(Sql2LongCondition.property("test").lowerOrEqualThan().value(123L).asString(), is("[test] <= 123"));
        assertThat(Sql2LongCondition.property("test").greaterOrEqualThan().value(123L).asString(), is("[test] >= 123"));
        assertThat(Sql2LongCondition.property("test").greaterThan().value(123L).asString(), is("[test] > 123"));
    }
}