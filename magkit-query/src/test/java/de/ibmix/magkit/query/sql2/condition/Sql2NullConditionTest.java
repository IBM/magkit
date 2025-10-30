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

import de.ibmix.magkit.query.sql2.statement.Sql2Statement;
import de.ibmix.magkit.query.sql2.statement.Sql2SelectorNames;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for Sql2NullCondition.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-06-17
 */
public class Sql2NullConditionTest {

    /**
     * Verify isNull creation renders correctly and ignores blank names.
     */
    @Test
    public void isNull() {
        assertEquals(EMPTY, Sql2NullCondition.isNull(null).asString());
        assertEquals(EMPTY, Sql2NullCondition.isNull(EMPTY).asString());
        assertEquals("[test] IS NULL", Sql2NullCondition.isNull("test").asString());
    }

    /**
     * Verify isNotNull creation renders correctly and ignores blank names.
     */
    @Test
    public void isNotNull() {
        assertEquals(EMPTY, Sql2NullCondition.isNotNull(null).asString());
        assertEquals(EMPTY, Sql2NullCondition.isNotNull(EMPTY).asString());
        assertEquals("[test] IS NOT NULL", Sql2NullCondition.isNotNull("test").asString());
    }

    /**
     * Verify integration with Sql2Statement builder without selector aliases.
     */
    @Test
    public void testSelectors() {
        Sql2JoinConstraint condition = Sql2NullCondition.isNull("test");
        assertEquals("SELECT [a],[b] FROM [aperto:test] WHERE [test] IS NULL", Sql2Statement.select("a", "b").from("aperto:test").whereAny(condition).build());
    }

    /**
     * Verify appendTo adds the from selector name prefix when provided.
     */
    @Test
    public void appendToWithFromSelector() {
        Sql2JoinConstraint condition = Sql2NullCondition.isNull("prop");
        StringBuilder sb = new StringBuilder();
        condition.appendTo(sb, mockSelectorNames("f", null));
        assertEquals("f.[prop] IS NULL", sb.toString());
    }

    /**
     * Verify appendTo uses the join selector when forJoin was called and renders IS NOT NULL.
     */
    @Test
    public void appendToWithJoinSelector() {
        Sql2JoinConstraint condition = Sql2NullCondition.isNotNull("prop").forJoin();
        StringBuilder sb = new StringBuilder();
        condition.appendTo(sb, mockSelectorNames("f", "j"));
        assertEquals("j.[prop] IS NOT NULL", sb.toString());
    }

    /**
     * Verify appendTo ignores empty property names (builder content unchanged).
     */
    @Test
    public void appendToBlankProperty() {
        Sql2JoinConstraint condition = Sql2NullCondition.isNull(null);
        StringBuilder sb = new StringBuilder("X");
        condition.appendTo(sb, mockSelectorNames("f", "j"));
        assertEquals("X", sb.toString());
    }

    /**
     * Verify appendTo without any selector names renders property only.
     */
    @Test
    public void appendToWithoutSelector() {
        Sql2JoinConstraint condition = Sql2NullCondition.isNull("prop");
        StringBuilder sb = new StringBuilder();
        condition.appendTo(sb, mockSelectorNames(null, null));
        assertEquals("[prop] IS NULL", sb.toString());
    }

    /**
     * Verify appendTo for join when join selector name is blank falls back to no prefix.
     */
    @Test
    public void appendToForJoinWithoutJoinSelector() {
        Sql2JoinConstraint condition = Sql2NullCondition.isNotNull("prop").forJoin();
        StringBuilder sb = new StringBuilder();
        condition.appendTo(sb, mockSelectorNames("from", null));
        assertEquals("[prop] IS NOT NULL", sb.toString());
    }

    /**
     * Verify isNotEmpty returns expected values for blank and non blank property names.
     */
    @Test
    public void isNotEmpty() {
        assertFalse(Sql2NullCondition.isNull(null).isNotEmpty());
        assertFalse(Sql2NullCondition.isNull(EMPTY).isNotEmpty());
        assertTrue(Sql2NullCondition.isNull("prop").isNotEmpty());
    }

    private Sql2SelectorNames mockSelectorNames(String fromName, String joinName) {
        Sql2SelectorNames selectorNames = mock(Sql2SelectorNames.class);
        when(selectorNames.getFromSelectorName()).thenReturn(fromName);
        when(selectorNames.getJoinSelectorName()).thenReturn(joinName);
        return selectorNames;
    }
}
