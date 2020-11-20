package com.aperto.magkit.query.sql2.condition;

import org.junit.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2NameCondition.
 *
 * @author wolf.bubenik@aperto.com
 * @since (11.11.2020)
 */
public class Sql2NameConditionTest {

    @Test
    public void lowerCase() {
        assertThat(new Sql2NameCondition().lowerCase().equalsAny().values().asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().lowerCase().equalsAny().values("").asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().lowerCase().equalsAny().values("value'_10%").asString(), is("lower(name()) = 'value''_10%'"));
    }

    @Test
    public void upperCase() {
        assertThat(new Sql2NameCondition().upperCase().equalsAny().values().asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().upperCase().equalsAny().values("").asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().upperCase().equalsAny().values("VALUE'_10%").asString(), is("upper(name()) = 'VALUE''_10%'"));
    }

    @Test
    public void isNotEmpty() {
        assertThat(new Sql2NameCondition().isNotEmpty(), is(false));
        assertThat(new Sql2NameCondition().values().isNotEmpty(), is(false));
        assertThat(new Sql2NameCondition().values(null, null).isNotEmpty(), is(false));
        assertThat(new Sql2NameCondition().values("", null).isNotEmpty(), is(true));
        assertThat(new Sql2NameCondition().values("test").isNotEmpty(), is(true));
    }

    @Test
    public void lowerThan() {
        assertThat(new Sql2NameCondition().lowerThan().value(null).asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().lowerThan().value("").asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().lowerThan().value("01").asString(), is("name() < '01'"));
    }

    @Test
    public void lowerOrEqualThan() {
        assertThat(new Sql2NameCondition().lowerOrEqualThan().value(null).asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().lowerOrEqualThan().value("").asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().lowerOrEqualThan().value("01").asString(), is("name() <= '01'"));
    }

    @Test
    public void equalsAny() {
        assertThat(new Sql2NameCondition().equalsAny().values().asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().equalsAny().values("").asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().equalsAny().values("value'_10%").asString(), is("name() = 'value''_10%'"));
        assertThat(new Sql2NameCondition().equalsAny().values("value'_10%", "other").asString(), is("(name() = 'value''_10%' OR name() = 'other')"));
    }

    @Test
    public void greaterOrEqualThan() {
        assertThat(new Sql2NameCondition().greaterOrEqualThan().value(null).asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().greaterOrEqualThan().value("").asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().greaterOrEqualThan().value("01").asString(), is("name() >= '01'"));
    }

    @Test
    public void greaterThan() {
        assertThat(new Sql2NameCondition().greaterThan().value(null).asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().greaterThan().value("").asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().greaterThan().value("01").asString(), is("name() > '01'"));
    }

    @Test
    public void excludeAny() {
        assertThat(new Sql2NameCondition().excludeAny().values().asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().excludeAny().values("").asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().excludeAny().values("value'_10%").asString(), is("name() <> 'value''_10%'"));
        assertThat(new Sql2NameCondition().excludeAny().values("value'_10%", "other").asString(), is("(name() <> 'value''_10%' OR name() <> 'other')"));
    }
}