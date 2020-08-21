package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.query.sql2.statement.Sql2Statement;
import org.junit.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2NullCondition.
 *
 * @author wolf.bubenik@aperto.com
 * @since (17.06.20)
 */
public class Sql2NullConditionTest {

    @Test
    public void isNull() {
        assertThat(Sql2NullCondition.isNull(null).asString(), is(EMPTY));
        assertThat(Sql2NullCondition.isNull(EMPTY).asString(), is(EMPTY));
        assertThat(Sql2NullCondition.isNull("test").asString(), is("[test] IS NULL"));
    }

    @Test
    public void isNotNull() {
        assertThat(Sql2NullCondition.isNotNull(null).asString(), is(EMPTY));
        assertThat(Sql2NullCondition.isNotNull(EMPTY).asString(), is(EMPTY));
        assertThat(Sql2NullCondition.isNotNull("test").asString(), is("[test] IS NOT NULL"));
    }

    @Test
    public void testSelectors() {
        Sql2JoinConstraint condition = Sql2NullCondition.isNull("test");
        assertThat(Sql2Statement.selectAttributes("a", "b").from("aperto:test").whereAny(condition).build(), is("SELECT [a],[b] FROM [aperto:test] WHERE [test] IS NULL"));
    }
}