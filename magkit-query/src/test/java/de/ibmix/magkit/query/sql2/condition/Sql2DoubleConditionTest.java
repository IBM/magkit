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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Sql2DoubleCondition.
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-24
 */
public class Sql2DoubleConditionTest {

    @Test
    public void property() {
        assertEquals("", Sql2DoubleCondition.property(null).equalsAll().values().asString());
        assertEquals("", Sql2DoubleCondition.property(null).equalsAll().values(1.23).asString());
        assertEquals("", Sql2DoubleCondition.property("").equalsAll().values(1.23).asString());
        assertEquals("([test] = 1.23 AND [test] = 2.34)", Sql2DoubleCondition.property("test").equalsAll().values(1.23, 2.34).asString());
        assertEquals("([test] = 1.23 OR [test] = 2.34)", Sql2DoubleCondition.property("test").equalsAny().values(1.23, 2.34).asString());
        assertEquals("([test] <> 1.23 AND [test] <> 2.34)", Sql2DoubleCondition.property("test").excludeAll().values(1.23, 2.34).asString());
        assertEquals("([test] <> 1.23 OR [test] <> 2.34)", Sql2DoubleCondition.property("test").excludeAny().values(1.23, 2.34).asString());
        assertEquals("[test] < 1.23", Sql2DoubleCondition.property("test").lowerThan().value(1.23).asString());
        assertEquals("[test] <= 1.23", Sql2DoubleCondition.property("test").lowerOrEqualThan().value(1.23).asString());
        assertEquals("[test] >= 1.23", Sql2DoubleCondition.property("test").greaterOrEqualThan().value(1.23).asString());
        assertEquals("[test] > 1.23", Sql2DoubleCondition.property("test").greaterThan().value(1.23).asString());
    }

    @Test
    public void appendValueConstraint() {
        StringBuilder result = new StringBuilder();
        Sql2DoubleCondition condition = (Sql2DoubleCondition) Sql2DoubleCondition.property("test").greaterThan();

        condition.appendValueConstraint(result, null, null, null);
        assertEquals("", result.toString());

        result = new StringBuilder();
        condition.appendValueConstraint(result, null, "test", 1.23D);
        assertEquals("[test] > 1.23", result.toString());

        result = new StringBuilder();
        condition.appendValueConstraint(result, "", "test", 1.23D);
        assertEquals("[test] > 1.23", result.toString());

        result = new StringBuilder();
        condition.appendValueConstraint(result, "  \t \r ", "test", 1.23D);
        assertEquals("[test] > 1.23", result.toString());

        result = new StringBuilder();
        condition.appendValueConstraint(result, "selector", "test", 1.23D);
        assertEquals("selector.[test] > 1.23", result.toString());
    }

    @Test
    public void bindVariable() {
        assertEquals("", Sql2DoubleCondition.property("test").equalsAny().bindVariable(null).asString());
        assertEquals("", Sql2DoubleCondition.property("test").equalsAny().bindVariable("").asString());
        assertEquals("", Sql2DoubleCondition.property("test").equalsAny().bindVariable(" \t\r ").asString());
        assertEquals("[test] = $value", Sql2DoubleCondition.property("test").equalsAny().bindVariable("value").asString());
        assertEquals("[test] = $value", Sql2DoubleCondition.property("test").equalsAny().bindVariable("  value\t").asString());
        assertEquals("[test] = $value", Sql2DoubleCondition.property("test").equalsAny().bindVariable("  $value\t").asString());
    }
}
