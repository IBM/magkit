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
    }

    @Test
    public void upperCase() {
        assertThat(new Sql2NameCondition().upperCase().equalsAny().values().asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().upperCase().equalsAny().values("").asString(), is(EMPTY));
        assertThat(new Sql2NameCondition().upperCase().equalsAny().values("value'_10%").asString(), is("upper(name()) = 'value''_10%'"));
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
        assertThat(new Sql2NameCondition().lowerThan().value("value'_10%").asString(), is("name() < 'value''_10%'"));
    }

    @Test
    public void lowerOrEqualThan() {
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
    }

    @Test
    public void greaterThan() {
    }

    @Test
    public void excludeAny() {
    }

    @Test
    public void values() {
    }

    @Test
    public void value() {
    }

    @Test
    public void appendTo() {
    }
}