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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Sql2NameCondition.
 * @author wolf.bubenik@ibmix.de
 * @since 2020-11-11
 */
public class Sql2NameConditionTest {

    /**
     * Verifies lower case transformation with equalsAny including escaping and empty input handling.
     */
    @Test
    public void lowerCase() {
        assertEquals(EMPTY, new Sql2NameCondition().lowerCase().equalsAny().values().asString());
        assertEquals(EMPTY, new Sql2NameCondition().lowerCase().equalsAny().values("").asString());
        assertEquals("lower(name()) = 'value''_10%'", new Sql2NameCondition().lowerCase().equalsAny().values("value'_10%").asString());
    }

    /**
     * Verifies upper case transformation with equalsAny including escaping and empty input handling.
     */
    @Test
    public void upperCase() {
        assertEquals(EMPTY, new Sql2NameCondition().upperCase().equalsAny().values().asString());
        assertEquals(EMPTY, new Sql2NameCondition().upperCase().equalsAny().values("").asString());
        assertEquals("upper(name()) = 'VALUE''_10%'", new Sql2NameCondition().upperCase().equalsAny().values("VALUE'_10%").asString());
    }

    /**
     * Verifies the isNotEmpty flag behavior for various value configurations.
     */
    @Test
    public void isNotEmpty() {
        assertFalse(new Sql2NameCondition().isNotEmpty());
        assertFalse(new Sql2NameCondition().values().isNotEmpty());
        assertFalse(new Sql2NameCondition().values(null, null).isNotEmpty());
        assertTrue(new Sql2NameCondition().values("", null).isNotEmpty());
        assertTrue(new Sql2NameCondition().values("test").isNotEmpty());
    }

    /**
     * Verifies lowerThan comparison including empty and null value handling.
     */
    @Test
    public void lowerThan() {
        assertEquals(EMPTY, new Sql2NameCondition().lowerThan().value(null).asString());
        assertEquals(EMPTY, new Sql2NameCondition().lowerThan().value("").asString());
        assertEquals("name() < '01'", new Sql2NameCondition().lowerThan().value("01").asString());
    }

    /**
     * Verifies lowerOrEqualThan comparison including empty and null value handling.
     */
    @Test
    public void lowerOrEqualThan() {
        assertEquals(EMPTY, new Sql2NameCondition().lowerOrEqualThan().value(null).asString());
        assertEquals(EMPTY, new Sql2NameCondition().lowerOrEqualThan().value("").asString());
        assertEquals("name() <= '01'", new Sql2NameCondition().lowerOrEqualThan().value("01").asString());
    }

    /**
     * Verifies equalsAny rendering for single and multi values including escaping and blank handling.
     */
    @Test
    public void equalsAny() {
        assertEquals(EMPTY, new Sql2NameCondition().equalsAny().values().asString());
        assertEquals(EMPTY, new Sql2NameCondition().equalsAny().values("").asString());
        assertEquals("name() = 'value''_10%'", new Sql2NameCondition().equalsAny().values("value'_10%").asString());
        assertEquals(
            "(name() = 'value''_10%' OR name() = 'other')",
            new Sql2NameCondition().equalsAny().values("value'_10%", "other").asString()
        );
    }

    /**
     * Verifies greaterOrEqualThan comparison including empty and null value handling.
     */
    @Test
    public void greaterOrEqualThan() {
        assertEquals(EMPTY, new Sql2NameCondition().greaterOrEqualThan().value(null).asString());
        assertEquals(EMPTY, new Sql2NameCondition().greaterOrEqualThan().value("").asString());
        assertEquals("name() >= '01'", new Sql2NameCondition().greaterOrEqualThan().value("01").asString());
    }

    /**
     * Verifies greaterThan comparison including empty and null value handling.
     */
    @Test
    public void greaterThan() {
        assertEquals(EMPTY, new Sql2NameCondition().greaterThan().value(null).asString());
        assertEquals(EMPTY, new Sql2NameCondition().greaterThan().value("").asString());
        assertEquals("name() > '01'", new Sql2NameCondition().greaterThan().value("01").asString());
    }

    /**
     * Verifies excludeAny rendering for single and multi values including escaping and blank handling.
     */
    @Test
    public void excludeAny() {
        assertEquals(EMPTY, new Sql2NameCondition().excludeAny().values().asString());
        assertEquals(EMPTY, new Sql2NameCondition().excludeAny().values("").asString());
        assertEquals("name() <> 'value''_10%'", new Sql2NameCondition().excludeAny().values("value'_10%").asString());
        assertEquals(
            "(name() <> 'value''_10%' OR name() <> 'other')",
            new Sql2NameCondition().excludeAny().values("value'_10%", "other").asString()
        );
    }

    /**
     * Tests selection of join selector when forJoin() is used for single value.
     */
    @Test
    public void forJoinSingleValue() {
        assertEquals(
            "name(j) = 'test'",
            new Sql2NameCondition().equalsAny().values("test").forJoin().asString("f", "j")
        );
    }

    /**
     * Tests selection of join selector with upper case transform and multi values.
     */
    @Test
    public void forJoinUpperCaseMultiValue() {
        assertEquals(
            "(upper(name(j)) = 'Home' OR upper(name(j)) = 'About')",
            new Sql2NameCondition().upperCase().equalsAny().values("Home", "About").forJoin().asString("fromSel", "j")
        );
    }

    /**
     * Tests multi value rendering where the first value is blank and second value non blank producing a leading operator.
     */
    @Test
    public void equalsAnyFirstBlankSecondValue() {
        assertEquals("( OR name() = 'B')", new Sql2NameCondition().equalsAny().values("", "B").asString());
    }

    /**
     * Tests multi value rendering where only blank values exist (first blank, second null) resulting in empty output.
     */
    @Test
    public void equalsAnyBlankAndNullValues() {
        assertEquals(EMPTY, new Sql2NameCondition().equalsAny().values("", null).asString());
    }

    /**
     * Verifies excludeAny multi value rendering with join selector.
     */
    @Test
    public void forJoinExcludeAnyMultiValue() {
        assertEquals(
            "(name(j) <> 'A' OR name(j) <> 'B')",
            new Sql2NameCondition().excludeAny().values("A", "B").forJoin().asString("fs", "j")
        );
    }

    /**
     * Verifies greaterThan comparison combined with upper case transformation on join selector.
     */
    @Test
    public void forJoinUpperCaseGreaterThanSingleValue() {
        assertEquals(
            "upper(name(j)) > 'X'",
            new Sql2NameCondition().upperCase().greaterThan().value("X").forJoin().asString("from", "j")
        );
    }

    /**
     * Verifies equalsAny multi value rendering combined with lower case transformation on join selector.
     */
    @Test
    public void forJoinLowerCaseEqualsAnyMultiValue() {
        assertEquals(
            "(lower(name(j)) = 'home' OR lower(name(j)) = 'about')",
            new Sql2NameCondition().lowerCase().equalsAny().values("home", "about").forJoin().asString("main", "j")
        );
    }

    /**
     * Verifies forJoin rendering when join selector name is null resulting in empty selector usage.
     */
    @Test
    public void forJoinNullJoinSelector() {
        assertEquals(
            "name() = 'x'",
            new Sql2NameCondition().equalsAny().values("x").forJoin().asString("from", null)
        );
    }

    /**
     * Verifies excludeAny multi value rendering where first value blank and second non blank produces leading operator.
     */
    @Test
    public void excludeAnyFirstBlankSecondValue() {
        assertEquals("( OR name() <> 'B')", new Sql2NameCondition().excludeAny().values("", "B").asString());
    }

    /**
     * Verifies multi value rendering where second value is blank leading to trailing operator without operand.
     */
    @Test
    public void equalsAnySecondBlankValue() {
        assertEquals("(name() = 'A' OR )", new Sql2NameCondition().equalsAny().values("A", "").asString());
    }

    /**
     * Verifies that a first null value suppresses rendering even if a second non null value exists.
     */
    @Test
    public void equalsAnyFirstNullSecondValue() {
        assertEquals(EMPTY, new Sql2NameCondition().equalsAny().values(null, "B").asString());
    }

    /**
     * Verifies excludeAny combined with lower case transformation for single value.
     */
    @Test
    public void lowerCaseExcludeAnySingleValue() {
        assertEquals("lower(name()) <> 'x'", new Sql2NameCondition().lowerCase().excludeAny().values("x").asString());
    }

    /**
     * Verifies multi value rendering with first blank and second containing quotes for equalsAny after upper case transform.
     */
    @Test
    public void upperCaseEqualsAnyFirstBlankSecondQuoted() {
        assertEquals(
            "( OR upper(name()) = 'val''ue')",
            new Sql2NameCondition().upperCase().equalsAny().values("", "val'ue").asString()
        );
    }
}
