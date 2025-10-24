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
 * Tests for Sql2ConstraintGroup.
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-05
 */
public class Sql2ConstraintGroupTest {

    @Test
    public void and() {
        assertEquals(EMPTY, Sql2ConstraintGroup.and().asString());
        assertEquals("[test] = 'value'", Sql2ConstraintGroup.and().matches(Sql2StringCondition.property("test").equalsAny().values("value")).asString());
        assertEquals("([test] = 'value' AND [test2] = 'other')", Sql2ConstraintGroup.and().matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            .asString());

        assertEquals("(([test] = 'value' OR [test2] = 'other') AND ([test] = 'value' OR [test2] = 'other'))", Sql2ConstraintGroup.and()
            .matches(
                Sql2ConstraintGroup.or()
                    .matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other")),
                Sql2ConstraintGroup.or()
                    .matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            )
            .asString());
    }

    @Test
    public void or() {
        assertEquals(EMPTY, Sql2ConstraintGroup.or().asString());
        assertEquals("[test] = 'value'", Sql2ConstraintGroup.or().matches(Sql2StringCondition.property("test").equalsAny().values("value")).asString());
        assertEquals("([test] = 'value' OR [test2] = 'other')", Sql2ConstraintGroup.or().matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            .asString());

        assertEquals("(([test] = 'value' AND [test2] = 'other') OR ([test] = 'value' AND [test2] = 'other'))", Sql2ConstraintGroup.or()
            .matches(
                Sql2ConstraintGroup.and()
                    .matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other")),
                Sql2ConstraintGroup.and()
                    .matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            )
            .asString());
    }

    @Test
    public void not() {
        assertEquals(EMPTY, Sql2ConstraintGroup.and().not().asString());
        assertEquals(EMPTY, Sql2ConstraintGroup.or().not().asString());
        assertEquals("not([test] = 'value')", Sql2ConstraintGroup.and().not().matches(Sql2StringCondition.property("test").equalsAny().values("value")).asString());
        assertEquals("not([test] = 'value')", Sql2ConstraintGroup.or().not().matches(Sql2StringCondition.property("test").equalsAny().values("value")).asString());
        assertEquals("not([test] = 'value' AND [test2] = 'other')", Sql2ConstraintGroup.and().not().matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            .asString());
        assertEquals("not([test] = 'value' OR [test2] = 'other')", Sql2ConstraintGroup.or().not().matches(Sql2StringCondition.property("test").equalsAny().values("value"), Sql2StringCondition.property("test2").equalsAny().values("other"))
            .asString());
    }
}
