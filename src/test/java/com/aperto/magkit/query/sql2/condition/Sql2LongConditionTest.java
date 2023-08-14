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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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

    @Test
    public void appendValueConstraint() {
        StringBuilder result = new StringBuilder();
        Sql2LongCondition condition = (Sql2LongCondition) Sql2LongCondition.property("test").greaterThan();

        condition.appendValueConstraint(result, null, null, null);
        assertThat(result.toString(), is(""));

        result = new StringBuilder();
        condition.appendValueConstraint(result, null, "test", 123L);
        assertThat(result.toString(), is("[test] > 123"));

        result = new StringBuilder();
        condition.appendValueConstraint(result, "", "test", 123L);
        assertThat(result.toString(), is("[test] > 123"));

        result = new StringBuilder();
        condition.appendValueConstraint(result, "  \t \r ", "test", 123L);
        assertThat(result.toString(), is("[test] > 123"));

        result = new StringBuilder();
        condition.appendValueConstraint(result, "selector", "test", 123L);
        assertThat(result.toString(), is("selector.[test] > 123"));
    }

    @Test
    public void values() {
        assertThat(Sql2LongCondition.property("test").equalsAny().values((Long[]) null).asString(), is(""));
        assertThat(Sql2LongCondition.property("test").equalsAny().values(null, 2L).asString(), is(""));
        assertThat(Sql2LongCondition.property("test").equalsAny().values(1L).asString(), is("[test] = 1"));
        assertThat(Sql2LongCondition.property("test").equalsAny().values(1L, null).asString(), is("[test] = 1"));
        assertThat(Sql2LongCondition.property("test").equalsAny().values(1L, null, 3L).asString(), is("[test] = 1"));
        assertThat(Sql2LongCondition.property("test").equalsAny().values(1L, 2L, 3L).asString(), is("([test] = 1 OR [test] = 2 OR [test] = 3)"));
    }
}
