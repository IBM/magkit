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
        // intentionally empty test (existing placeholder)
    }

    @Test
    public void lowerCase() {
        assertEquals("lower([test]) = 'value'", Sql2StringCondition.property("test").lowerCase().equalsAny().values("value").asString());
        assertEquals("not(lower([test]) = 'value')", Sql2StringCondition.property("test").lowerCase().not().equalsAny().values("value").asString());
    }

    @Test
    public void upperCase() {
        assertEquals("upper([test]) = 'value'", Sql2StringCondition.property("test").upperCase().equalsAny().values("value").asString());
        assertEquals("not(upper([test]) = 'value')", Sql2StringCondition.property("test").upperCase().not().equalsAny().values("value").asString());
    }
}
