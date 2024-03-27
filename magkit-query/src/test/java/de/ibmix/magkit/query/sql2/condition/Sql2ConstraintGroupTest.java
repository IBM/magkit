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

import org.junit.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test Sql2ConstraintGroup.
 *
 * @author wolf.bubenik@ibmix.de
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
