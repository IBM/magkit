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

    @Test
    public void appendValueConstraint() {
        StringBuilder result = new StringBuilder();
        Sql2DoubleCondition condition = (Sql2DoubleCondition) Sql2DoubleCondition.property("test").greaterThan();

        condition.appendValueConstraint(result, null, null, null);
        assertThat(result.toString(), is(""));

        result = new StringBuilder();
        condition.appendValueConstraint(result, null, "test", 1.23D);
        assertThat(result.toString(), is("[test] > 1.23"));

        result = new StringBuilder();
        condition.appendValueConstraint(result, "", "test", 1.23D);
        assertThat(result.toString(), is("[test] > 1.23"));

        result = new StringBuilder();
        condition.appendValueConstraint(result, "  \t \r ", "test", 1.23D);
        assertThat(result.toString(), is("[test] > 1.23"));

        result = new StringBuilder();
        condition.appendValueConstraint(result, "selector", "test", 1.23D);
        assertThat(result.toString(), is("selector.[test] > 1.23"));
    }

    @Test
    public void bindVariable() {
        assertThat(Sql2DoubleCondition.property("test").equalsAny().bindVariable(null).asString(), is(""));
        assertThat(Sql2DoubleCondition.property("test").equalsAny().bindVariable("").asString(), is(""));
        assertThat(Sql2DoubleCondition.property("test").equalsAny().bindVariable(" \t\r ").asString(), is(""));
        assertThat(Sql2DoubleCondition.property("test").equalsAny().bindVariable("value").asString(), is("[test] = $value"));
        assertThat(Sql2DoubleCondition.property("test").equalsAny().bindVariable("  value\t").asString(), is("[test] = $value"));
        assertThat(Sql2DoubleCondition.property("test").equalsAny().bindVariable("  $value\t").asString(), is("[test] = $value"));
    }
}