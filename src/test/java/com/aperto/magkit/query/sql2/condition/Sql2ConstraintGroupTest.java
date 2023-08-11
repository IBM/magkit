package com.aperto.magkit.query.sql2.condition;

import org.junit.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2ConstraintGroup.
 *
 * @author wolf.bubenik@aperto.com
 * @since 05.04.2020
 */
public class Sql2ConstraintGroupTest {

    @Test
    public void and() {
        assertThat(Sql2ConstraintGroup.and().asString(), is(EMPTY));
        assertThat(Sql2ConstraintGroup.and().matches(Sql2StringCondition.property("test").equalsAny().values("value")).asString(), is("[test] = 'value'"));
        assertThat(Sql2ConstraintGroup.and().matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            .asString(), is("([test] = 'value' AND [test2] = 'other')"));

        assertThat(Sql2ConstraintGroup.and()
            .matches(
                Sql2ConstraintGroup.or()
                    .matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other")),
                Sql2ConstraintGroup.or()
                    .matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            )
            .asString(), is("(([test] = 'value' OR [test2] = 'other') AND ([test] = 'value' OR [test2] = 'other'))"));
    }

    @Test
    public void or() {
        assertThat(Sql2ConstraintGroup.or().asString(), is(EMPTY));
        assertThat(Sql2ConstraintGroup.or().matches(Sql2StringCondition.property("test").equalsAny().values("value")).asString(), is("[test] = 'value'"));
        assertThat(Sql2ConstraintGroup.or().matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            .asString(), is("([test] = 'value' OR [test2] = 'other')"));

        assertThat(Sql2ConstraintGroup.or()
            .matches(
                Sql2ConstraintGroup.and()
                    .matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other")),
                Sql2ConstraintGroup.and()
                    .matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            )
            .asString(), is("(([test] = 'value' AND [test2] = 'other') OR ([test] = 'value' AND [test2] = 'other'))"));
    }

    @Test
    public void not() {
        assertThat(Sql2ConstraintGroup.and().not().asString(), is(EMPTY));
        assertThat(Sql2ConstraintGroup.or().not().asString(), is(EMPTY));
        assertThat(Sql2ConstraintGroup.and().not().matches(Sql2StringCondition.property("test").equalsAny().values("value")).asString(), is("not([test] = 'value')"));
        assertThat(Sql2ConstraintGroup.or().not().matches(Sql2StringCondition.property("test").equalsAny().values("value")).asString(), is("not([test] = 'value')"));
        assertThat(Sql2ConstraintGroup.and().not().matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            .asString(), is("not([test] = 'value' AND [test2] = 'other')"));
        assertThat(Sql2ConstraintGroup.or().not().matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            .asString(), is("not([test] = 'value' OR [test2] = 'other')"));
    }
}