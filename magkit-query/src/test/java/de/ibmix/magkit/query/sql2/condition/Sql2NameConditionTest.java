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

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Sql2NameCondition.
 * @author wolf.bubenik@ibmix.de
 * @since 2020-11-11
 */
public class Sql2NameConditionTest {

    @Test
    public void lowerCase() {
        assertEquals(EMPTY, new Sql2NameCondition().lowerCase().equalsAny().values().asString());
        assertEquals(EMPTY, new Sql2NameCondition().lowerCase().equalsAny().values("").asString());
        assertEquals("lower(name()) = 'value''_10%'", new Sql2NameCondition().lowerCase().equalsAny().values("value'_10%").asString());
    }

    @Test
    public void upperCase() {
        assertEquals(EMPTY, new Sql2NameCondition().upperCase().equalsAny().values().asString());
        assertEquals(EMPTY, new Sql2NameCondition().upperCase().equalsAny().values("").asString());
        assertEquals("upper(name()) = 'VALUE''_10%'", new Sql2NameCondition().upperCase().equalsAny().values("VALUE'_10%").asString());
    }

    @Test
    public void isNotEmpty() {
        assertFalse(new Sql2NameCondition().isNotEmpty());
        assertFalse(new Sql2NameCondition().values().isNotEmpty());
        assertFalse(new Sql2NameCondition().values(null, null).isNotEmpty());
        assertTrue(new Sql2NameCondition().values("", null).isNotEmpty());
        assertTrue(new Sql2NameCondition().values("test").isNotEmpty());
    }

    @Test
    public void lowerThan() {
        assertEquals(EMPTY, new Sql2NameCondition().lowerThan().value(null).asString());
        assertEquals(EMPTY, new Sql2NameCondition().lowerThan().value("").asString());
        assertEquals("name() < '01'", new Sql2NameCondition().lowerThan().value("01").asString());
    }

    @Test
    public void lowerOrEqualThan() {
        assertEquals(EMPTY, new Sql2NameCondition().lowerOrEqualThan().value(null).asString());
        assertEquals(EMPTY, new Sql2NameCondition().lowerOrEqualThan().value("").asString());
        assertEquals("name() <= '01'", new Sql2NameCondition().lowerOrEqualThan().value("01").asString());
    }

    @Test
    public void equalsAny() {
        assertEquals(EMPTY, new Sql2NameCondition().equalsAny().values().asString());
        assertEquals(EMPTY, new Sql2NameCondition().equalsAny().values("").asString());
        assertEquals("name() = 'value''_10%'", new Sql2NameCondition().equalsAny().values("value'_10%").asString());
        assertEquals("(name() = 'value''_10%' OR name() = 'other')", new Sql2NameCondition().equalsAny().values("value'_10%", "other").asString());
    }

    @Test
    public void greaterOrEqualThan() {
        assertEquals(EMPTY, new Sql2NameCondition().greaterOrEqualThan().value(null).asString());
        assertEquals(EMPTY, new Sql2NameCondition().greaterOrEqualThan().value("").asString());
        assertEquals("name() >= '01'", new Sql2NameCondition().greaterOrEqualThan().value("01").asString());
    }

    @Test
    public void greaterThan() {
        assertEquals(EMPTY, new Sql2NameCondition().greaterThan().value(null).asString());
        assertEquals(EMPTY, new Sql2NameCondition().greaterThan().value("").asString());
        assertEquals("name() > '01'", new Sql2NameCondition().greaterThan().value("01").asString());
    }

    @Test
    public void excludeAny() {
        assertEquals(EMPTY, new Sql2NameCondition().excludeAny().values().asString());
        assertEquals(EMPTY, new Sql2NameCondition().excludeAny().values("").asString());
        assertEquals("name() <> 'value''_10%'", new Sql2NameCondition().excludeAny().values("value'_10%").asString());
        assertEquals("(name() <> 'value''_10%' OR name() <> 'other')", new Sql2NameCondition().excludeAny().values("value'_10%", "other").asString());
    }
}
