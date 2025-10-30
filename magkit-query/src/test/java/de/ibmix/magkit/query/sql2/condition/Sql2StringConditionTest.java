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
 * Tests for Sql2StringCondition.
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-02
 */
public class Sql2StringConditionTest {

    @Test
    public void property() {
        assertEquals(EMPTY, Sql2StringCondition.property("test").excludeAll().values().asString());
        assertEquals("[test] LIKE '%end'", Sql2StringCondition.property("test").endsWithAny().values("end").asString());
        assertEquals("not([test] LIKE '%end')", Sql2StringCondition.property("test").not().endsWithAny().values("end").asString());
    }

    @Test
    public void template() {
        assertEquals(EMPTY, Sql2StringCondition.template().equalsAny().values().asString());
        assertEquals("[mgnl:template] LIKE 'start%'", Sql2StringCondition.template().startsWithAny().values("start").asString());
        assertEquals("not([mgnl:template] LIKE 'start%')", Sql2StringCondition.template().not().startsWithAny().values("start").asString());
    }

    @Test
    public void identifier() {
        assertEquals(EMPTY, Sql2StringCondition.identifier().equalsAll().values().asString());
        assertEquals("([jcr:uuid] LIKE '%one%' AND [jcr:uuid] LIKE '%other%')", Sql2StringCondition.identifier().likeAll().values("one", "other").asString());
        assertEquals("not([jcr:uuid] LIKE '%one%' AND [jcr:uuid] LIKE '%other%')", Sql2StringCondition.identifier().not().likeAll().values("one", "other").asString());
    }

    @Test
    public void equalsAny() {
        assertEquals(EMPTY, Sql2StringCondition.property("key").equalsAny().values().asString());
        assertEquals("[key] = ''", Sql2StringCondition.property("key").equalsAny().values("").asString());
        assertEquals("[key] = 'value''_10%'", Sql2StringCondition.property("key").equalsAny().values("value'_10%").asString());
        assertEquals("not([key] = 'value''_10%')", Sql2StringCondition.property("key").not().equalsAny().values("value'_10%").asString());
        assertEquals("([key] = 'value''_10%' OR [key] = 'other')", Sql2StringCondition.property("key").equalsAny().values("value'_10%", "other").asString());
        assertEquals("not([key] = 'value''_10%' OR [key] = 'other')", Sql2StringCondition.property("key").not().equalsAny().values("value'_10%", "other").asString());
    }

    @Test
    public void startsWithAny() {
        assertEquals(EMPTY, Sql2StringCondition.template().startsWithAny().values().asString());
        assertEquals("[mgnl:template] LIKE ' %'", Sql2StringCondition.template().startsWithAny().values(" ").asString());
        assertEquals("[mgnl:template] LIKE 'value''\\_10\\%%'", Sql2StringCondition.template().startsWithAny().values("value'_10%").asString());
        assertEquals("not([mgnl:template] LIKE 'value''\\_10\\%%')", Sql2StringCondition.template().not().startsWithAny().values("value'_10%").asString());
        assertEquals("([mgnl:template] LIKE 'value''\\_10\\%%' OR [mgnl:template] LIKE 'other%')", Sql2StringCondition.template().startsWithAny().values("value'_10%", "other").asString());
        assertEquals("not([mgnl:template] LIKE 'value''\\_10\\%%' OR [mgnl:template] LIKE 'other%')", Sql2StringCondition.template().not().startsWithAny().values("value'_10%", "other").asString());
        assertEquals(EMPTY, Sql2StringCondition.property("x").startsWithAny().values("").asString());
        assertEquals("[x] LIKE 'start%'", Sql2StringCondition.property("x").startsWithAny().values("start", null).asString());
        assertEquals("lower([l]) LIKE '%v%'", Sql2StringCondition.property("l").lowerCase().likeAny().values("v").asString());
    }

    @Test
    public void endsWithAny() {
        assertEquals(EMPTY, Sql2StringCondition.identifier().endsWithAny().values().asString());
        assertEquals(EMPTY, Sql2StringCondition.identifier().endsWithAny().values("").asString());
        assertEquals("[jcr:uuid] LIKE '% '", Sql2StringCondition.identifier().endsWithAny().values(" ").asString());
        assertEquals("[jcr:uuid] LIKE '%value''\\_10\\%'", Sql2StringCondition.identifier().endsWithAny().values("value'_10%").asString());
        assertEquals("not([jcr:uuid] LIKE '%value''\\_10\\%')", Sql2StringCondition.identifier().not().endsWithAny().values("value'_10%").asString());
        assertEquals("([jcr:uuid] LIKE '%value''\\_10\\%' OR [jcr:uuid] LIKE '%other')", Sql2StringCondition.identifier().endsWithAny().values("value'_10%", "other").asString());
        assertEquals("not([jcr:uuid] LIKE '%value''\\_10\\%' OR [jcr:uuid] LIKE '%other')", Sql2StringCondition.identifier().not().endsWithAny().values("value'_10%", "other").asString());
    }

    @Test
    public void likeAny() {
        assertEquals(EMPTY, Sql2StringCondition.template().likeAny().values().asString());
        assertEquals(EMPTY, Sql2StringCondition.template().likeAny().values("").asString());
        assertEquals("[mgnl:template] LIKE '% %'", Sql2StringCondition.template().likeAny().values(" ").asString());
        assertEquals("[mgnl:template] LIKE '%value''\\_10\\%%'", Sql2StringCondition.template().likeAny().values("value'_10%").asString());
        assertEquals("not([mgnl:template] LIKE '%value''\\_10\\%%')", Sql2StringCondition.template().not().likeAny().values("value'_10%").asString());
        assertEquals("([mgnl:template] LIKE '%value''\\_10\\%%' OR [mgnl:template] LIKE '%other%')", Sql2StringCondition.template().likeAny().values("value'_10%", "other").asString());
        assertEquals("not([mgnl:template] LIKE '%value''\\_10\\%%' OR [mgnl:template] LIKE '%other%')", Sql2StringCondition.template().not().likeAny().values("value'_10%", "other").asString());
        assertEquals("(upper([u]) LIKE '%A%' OR upper([u]) LIKE '%B%')", Sql2StringCondition.property("u").upperCase().likeAny().values("A", "B").asString());
    }

    @Test
    public void likeAll() {
        assertEquals(EMPTY, Sql2StringCondition.template().likeAll().values().asString());
        assertEquals(EMPTY, Sql2StringCondition.template().likeAll().values("").asString());
        assertEquals("[mgnl:template] LIKE '% %'", Sql2StringCondition.template().likeAll().values(" ").asString());
        assertEquals("[mgnl:template] LIKE '%value''\\_10\\%%'", Sql2StringCondition.template().likeAll().values("value'_10%").asString());
        assertEquals("not([mgnl:template] LIKE '%value''\\_10\\%%')", Sql2StringCondition.template().not().likeAll().values("value'_10%").asString());
        assertEquals("([mgnl:template] LIKE '%value''\\_10\\%%' AND [mgnl:template] LIKE '%other%')", Sql2StringCondition.template().likeAll().values("value'_10%", "other").asString());
        assertEquals("not([mgnl:template] LIKE '%value''\\_10\\%%' AND [mgnl:template] LIKE '%other%')", Sql2StringCondition.template().not().likeAll().values("value'_10%", "other").asString());
    }

    @Test
    public void length() {
        assertNull(Sql2StringCondition.property("test").length());
    }

    @Test
    public void lowerCase() {
        assertEquals("lower([test]) = 'value'", Sql2StringCondition.property("test").lowerCase().equalsAny().values("value").asString());
        assertEquals("not(lower([test]) = 'value')", Sql2StringCondition.property("test").lowerCase().not().equalsAny().values("value").asString());
        assertEquals("(lower([test]) = 'a' OR lower([test]) = 'b')", Sql2StringCondition.property("test").lowerCase().equalsAny().values("a", "b").asString());
    }

    @Test
    public void upperCase() {
        assertEquals("upper([test]) = 'value'", Sql2StringCondition.property("test").upperCase().equalsAny().values("value").asString());
        assertEquals("not(upper([test]) = 'value')", Sql2StringCondition.property("test").upperCase().not().equalsAny().values("value").asString());
    }

    /**
     * Additional coverage for equalsAll and exclude operators including NOT and AND semantics.
     */
    @Test
    public void equalsAllAndExcludeVariants() {
        assertEquals("([k] = 'a' AND [k] = 'b')", Sql2StringCondition.property("k").equalsAll().values("a", "b").asString());
        assertEquals("not([k] = 'a' AND [k] = 'b')", Sql2StringCondition.property("k").not().equalsAll().values("a", "b").asString());
        assertEquals("([k] <> 'a' OR [k] <> 'b')", Sql2StringCondition.property("k").excludeAny().values("a", "b").asString());
        assertEquals("([k] <> 'a' AND [k] <> 'b')", Sql2StringCondition.property("k").excludeAll().values("a", "b").asString());
        assertEquals("not([k] <> 'a' AND [k] <> 'b')", Sql2StringCondition.property("k").not().excludeAll().values("a", "b").asString());
        assertEquals("[s] <> 'x'", Sql2StringCondition.property("s").excludeAny().values("x").asString());
    }

    /**
     * Tests for comparison operators lower/greater and join selector usage.
     */
    @Test
    public void comparisonOperatorsAndJoinSelector() {
        assertEquals("[a] < 'b'", Sql2StringCondition.property("a").lowerThan().value("b").asString());
        assertEquals("[a] <= 'b'", Sql2StringCondition.property("a").lowerOrEqualThan().value("b").asString());
        assertEquals("[a] >= 'b'", Sql2StringCondition.property("a").greaterOrEqualThan().value("b").asString());
        assertEquals("[a] > 'b'", Sql2StringCondition.property("a").greaterThan().value("b").asString());
        assertEquals("x.[prop] = 'y'", Sql2StringCondition.property("prop").equalsAny().values("y").asString("x", null));
        assertEquals("j.[prop] = 'y'", Sql2StringCondition.property("prop").equalsAny().values("y").forJoin().asString("x", "j"));
        assertEquals("j.[prop] LIKE '%a%'", Sql2StringCondition.property("prop").likeAny().values("a").forJoin().asString("x", "j"));
        assertEquals("(j.[prop] LIKE '%a%' AND j.[prop] LIKE '%b%')", Sql2StringCondition.property("prop").likeAll().values("a", "b").forJoin().asString("x", "j"));
    }

    /**
     * Tests for bind variable rendering including automatic $ prefix and NOT + multi-value parenthesis interaction.
     */
    @Test
    public void bindVariableVariants() {
        assertEquals("[p] = $var", Sql2StringCondition.property("p").equalsAny().bindVariable("var").asString());
        assertEquals("[p] = $var", Sql2StringCondition.property("p").equalsAny().bindVariable("$var").asString());
        assertEquals("s.[p] = $var", Sql2StringCondition.property("p").equalsAny().bindVariable("var").asString("s", null));
        assertEquals("j.[p] = $var", Sql2StringCondition.property("p").equalsAny().bindVariable("var").forJoin().asString("s", "j"));
        assertEquals("not([p] = $var)", Sql2StringCondition.property("p").not().equalsAny().bindVariable("var").asString());
        assertEquals(EMPTY, Sql2StringCondition.property("").equalsAny().bindVariable("var").asString());
    }

    /**
     * Tests for null value handling resulting in empty output and isNotEmpty semantics.
     */
    @Test
    public void nullValueHandling() {
        assertEquals(EMPTY, Sql2StringCondition.property("n").equalsAny().values((String) null).asString());
        assertEquals("[n] = 'a'", Sql2StringCondition.property("n").equalsAny().values("a", null).asString());
        assertFalse(Sql2StringCondition.property(" ").equalsAny().values("x").isNotEmpty());
        assertFalse(Sql2StringCondition.property(null).equalsAny().values("x").isNotEmpty());
        assertFalse(Sql2StringCondition.property("p").equalsAny().bindVariable("").isNotEmpty());
        assertEquals(EMPTY, Sql2StringCondition.property("").equalsAny().values("x").asString());
        assertEquals(EMPTY, Sql2StringCondition.property(null).equalsAny().values("x").asString());
    }
}
