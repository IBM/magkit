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

    private static final Sql2JoinConstraint EMPTY_CONSTRAINT = new Sql2JoinConstraint() {
        @Override
        public Sql2JoinConstraint forJoin() {
            return this;
        }
        @Override
        public void appendTo(StringBuilder sql2, de.ibmix.magkit.query.sql2.statement.Sql2SelectorNames selectorNames) {
        }
        @Override
        public boolean isNotEmpty() {
            return false;
        }
    };

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

    /**
     * Verify handling of null and empty arrays passed to matches().
     */
    @Test
    public void nullAndEmptyArray() {
        assertEquals(EMPTY, Sql2ConstraintGroup.and().matches((Sql2JoinConstraint[]) null).asString());
        assertEquals(EMPTY, Sql2ConstraintGroup.and().matches().asString());
        assertEquals(EMPTY, Sql2ConstraintGroup.and().matches().asString());
    }

    /**
     * Verify a single empty child constraint results in no output (no parentheses).
     */
    @Test
    public void singleEmptyConstraint() {
        assertEquals(EMPTY, Sql2ConstraintGroup.and().matches(EMPTY_CONSTRAINT).asString());
        assertEquals(EMPTY, Sql2ConstraintGroup.or().matches(EMPTY_CONSTRAINT).asString());
    }

    /**
     * Verify multiple empty child constraints produce an empty parenthesis block (current implementation) and with NOT wrapper.
     */
    @Test
    public void multipleEmptyConstraints() {
        assertEquals("()", Sql2ConstraintGroup.and().matches(EMPTY_CONSTRAINT, EMPTY_CONSTRAINT).asString());
        assertEquals("()", Sql2ConstraintGroup.or().matches(EMPTY_CONSTRAINT, EMPTY_CONSTRAINT).asString());
        assertEquals("not()", Sql2ConstraintGroup.and().not().matches(EMPTY_CONSTRAINT, EMPTY_CONSTRAINT).asString());
        assertEquals("not()", Sql2ConstraintGroup.or().not().matches(EMPTY_CONSTRAINT, EMPTY_CONSTRAINT).asString());
    }

    /**
     * Verify empty constraints are skipped and operators applied only between non-empty ones, preserving parentheses for original size > 1.
     */
    @Test
    public void skipEmptyConstraintsMixed() {
        assertEquals("([a] = '1')", Sql2ConstraintGroup.and().matches(EMPTY_CONSTRAINT, Sql2StringCondition.property("a").equalsAny().values("1"), EMPTY_CONSTRAINT).asString());
        assertEquals("([a] = '1' AND [b] = '2')", Sql2ConstraintGroup.and().matches(Sql2StringCondition.property("a").equalsAny().values("1"), EMPTY_CONSTRAINT, Sql2StringCondition.property("b").equalsAny().values("2")).asString());
        assertEquals("([a] = '1' OR [b] = '2')", Sql2ConstraintGroup.or().matches(Sql2StringCondition.property("a").equalsAny().values("1"), EMPTY_CONSTRAINT, Sql2StringCondition.property("b").equalsAny().values("2")).asString());
    }

    /**
     * Verify forJoin() propagation switches selector usage to join selector for all non-empty child constraints.
     */
    @Test
    public void forJoinPropagation() {
        assertEquals("f.[title] = 'Hello'", Sql2ConstraintGroup.and().matches(Sql2StringCondition.property("title").equalsAny().values("Hello")).asString("f", "j"));
        assertEquals("j.[title] = 'Hello'", Sql2ConstraintGroup.and().matches(Sql2StringCondition.property("title").equalsAny().values("Hello")).forJoin().asString("f", "j"));
        assertEquals("(j.[title] = 'Hello' AND j.[other] = 'World')", Sql2ConstraintGroup.and().matches(
            Sql2StringCondition.property("title").equalsAny().values("Hello"),
            Sql2StringCondition.property("other").equalsAny().values("World"),
            EMPTY_CONSTRAINT
        ).forJoin().asString("f", "j"));
    }

    /**
     * Verify isNotEmpty semantics for various matches inputs.
     */
    @Test
    public void isNotEmptySemantics() {
        Sql2ConstraintGroup g1 = Sql2ConstraintGroup.and();
        assertFalse(g1.isNotEmpty());
        Sql2ConstraintGroup g2 = Sql2ConstraintGroup.and().matches((Sql2JoinConstraint[]) null);
        assertFalse(g2.isNotEmpty());
        Sql2ConstraintGroup g3 = Sql2ConstraintGroup.and().matches();
        assertFalse(g3.isNotEmpty());
        Sql2ConstraintGroup g4 = Sql2ConstraintGroup.and().matches();
        assertFalse(g4.isNotEmpty());
        Sql2ConstraintGroup g5 = Sql2ConstraintGroup.and().matches(EMPTY_CONSTRAINT);
        assertTrue(g5.isNotEmpty());
    }
}
