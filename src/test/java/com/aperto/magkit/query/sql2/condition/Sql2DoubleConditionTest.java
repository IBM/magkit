package com.aperto.magkit.query.sql2.condition;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2DoubleCondition.
 *
 * @author wolf.bubenik@aperto.com
 * @since (24.04.2020)
 */
public class Sql2DoubleConditionTest {

    @Test
    public void property() {
        assertThat(Sql2DoubleCondition.property(null).equalsAll().values().asString(), is(""));
        assertThat(Sql2DoubleCondition.property(null).equalsAll().values(1.23).asString(), is(""));
        assertThat(Sql2DoubleCondition.property("").equalsAll().values(1.23).asString(), is(""));
        assertThat(Sql2DoubleCondition.property("test").equalsAll().values(1.23, 2.34).asString(), is("([test] = 1.23 AND [test] = 2.34)"));
        assertThat(Sql2DoubleCondition.property("test").equalsAny().values(1.23, 2.34).asString(), is("([test] = 1.23 OR [test] = 2.34)"));
        assertThat(Sql2DoubleCondition.property("test").excludeAll().values(1.23, 2.34).asString(), is("([test] <> 1.23 AND [test] <> 2.34)"));
        assertThat(Sql2DoubleCondition.property("test").excludeAny().values(1.23, 2.34).asString(), is("([test] <> 1.23 OR [test] <> 2.34)"));
        assertThat(Sql2DoubleCondition.property("test").lowerThan().value(1.23).asString(), is("[test] < 1.23"));
        assertThat(Sql2DoubleCondition.property("test").lowerOrEqualThan().value(1.23).asString(), is("[test] <= 1.23"));
        assertThat(Sql2DoubleCondition.property("test").greaterOrEqualThan().value(1.23).asString(), is("[test] >= 1.23"));
        assertThat(Sql2DoubleCondition.property("test").greaterThan().value(1.23).asString(), is("[test] > 1.23"));
    }
}