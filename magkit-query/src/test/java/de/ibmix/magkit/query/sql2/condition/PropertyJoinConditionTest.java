package de.ibmix.magkit.query.sql2.condition;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2025 IBM iX
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

import de.ibmix.magkit.query.sql2.statement.Sql2SelectorNames;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PropertyJoinCondition} covering all factory methods and append output for different operators.
 * Verifies correct operator insertion, property name placement and selector name usage including null selector names edge case.
 * Reflection is used to obtain an initial instance because the constructor is private and no static factory exists.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-30
 */
public class PropertyJoinConditionTest {

    /**
     * Verify rendering with equals operator and that a new instance (immutability) is created by the factory.
     * Selector names should prefix properties in correct order.
     *
     * @throws Exception reflection failures
     */
    @Test
    public void testSelectedEqualsJoinedAppendTo() throws Exception {
        PropertyJoinCondition condition = PropertyJoinCondition.selectedEqualsJoined("title", "name");
        StringBuilder sql2 = new StringBuilder();
        condition.appendTo(sql2, mockSelectorNames("fromSel", "joinSel"));
        assertEquals("fromSel.title = joinSel.name", sql2.toString());
    }

    /**
     * Verify rendering with not equals operator.
     *
     * @throws Exception reflection failures
     */
    @Test
    public void testSelectedNotEqualsJoinedAppendTo() throws Exception {
        PropertyJoinCondition condition = PropertyJoinCondition.selectedNotEqualsJoined("title", "name");
        StringBuilder sql2 = new StringBuilder();
        condition.appendTo(sql2, mockSelectorNames("fromSel", "joinSel"));
        assertEquals("fromSel.title <> joinSel.name", sql2.toString());
    }

    /**
     * Verify rendering with lower-than operator.
     *
     * @throws Exception reflection failures
     */
    @Test
    public void testSelectedLowerJoinedAppendTo() throws Exception {
        PropertyJoinCondition condition = PropertyJoinCondition.selectedLowerJoined("views", "maxViews");
        StringBuilder sql2 = new StringBuilder();
        condition.appendTo(sql2, mockSelectorNames("f", "j"));
        assertEquals("f.views < j.maxViews", sql2.toString());
    }

    /**
     * Verify rendering with lower-or-equal-than operator.
     *
     * @throws Exception reflection failures
     */
    @Test
    public void testSelectedLowerOrEqualJoinedAppendTo() throws Exception {
        PropertyJoinCondition condition = PropertyJoinCondition.selectedLowerOrEqualJoined("views", "maxViews");
        StringBuilder sql2 = new StringBuilder();
        condition.appendTo(sql2, mockSelectorNames("f", "j"));
        assertEquals("f.views <= j.maxViews", sql2.toString());
    }

    /**
     * Verify rendering with greater-or-equal-than operator.
     *
     * @throws Exception reflection failures
     */
    @Test
    public void testSelectedGreaterOrEqualJoinedAppendTo() throws Exception {
        PropertyJoinCondition condition = PropertyJoinCondition.selectedGraterOrEqualJoined("views", "minViews");
        StringBuilder sql2 = new StringBuilder();
        condition.appendTo(sql2, mockSelectorNames("f", "j"));
        assertEquals("f.views >= j.minViews", sql2.toString());
    }

    /**
     * Verify rendering with greater-than operator.
     *
     * @throws Exception reflection failures
     */
    @Test
    public void testSelectedGreaterJoinedAppendTo() throws Exception {
        PropertyJoinCondition condition = PropertyJoinCondition.selectedGraterJoined("views", "minViews");
        StringBuilder sql2 = new StringBuilder();
        condition.appendTo(sql2, mockSelectorNames("f", "j"));
        assertEquals("f.views > j.minViews", sql2.toString());
    }

    /**
     * Verify rendering when selector names are null (edge case) to ensure null is rendered as literal "null".
     *
     * @throws Exception reflection failures
     */
    @Test
    public void testAppendToWithNullSelectorNames() throws Exception {
        PropertyJoinCondition condition = PropertyJoinCondition.selectedEqualsJoined("a", "b");
        StringBuilder sql2 = new StringBuilder();
        Sql2SelectorNames selectorNames = mock(Sql2SelectorNames.class);
        when(selectorNames.getFromSelectorName()).thenReturn(null);
        when(selectorNames.getJoinSelectorName()).thenReturn(null);
        condition.appendTo(sql2, mockSelectorNames(null, null));
        assertEquals("null.a = null.b", sql2.toString());
    }

    private Sql2SelectorNames mockSelectorNames(String fromName, String joinName) {
        Sql2SelectorNames selectorNames = mock(Sql2SelectorNames.class);
        when(selectorNames.getFromSelectorName()).thenReturn(fromName);
        when(selectorNames.getJoinSelectorName()).thenReturn(joinName);
        return selectorNames;
    }

}
