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

import de.ibmix.magkit.query.sql2.statement.Sql2SelectorNames;

/**
 * Builder for SQL2 join conditions comparing properties of the selected (FROM) selector and the joined selector.
 * Provides convenience factory methods for the supported comparison operators and stores both property names and
 * the comparison operator until rendering via {@link #appendTo(StringBuilder, Sql2SelectorNames)}.
 *
 * Thread-safety: Not thread safe.
 * Null handling: Callers are responsible for non-null property names; no validation performed.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-18
 */
public final class PropertyJoinCondition implements Sql2JoinCondition {

    private final String _selectedPropertyName;
    private final String _joinedPropertyName;
    private final String _operation;

    private PropertyJoinCondition(final String selectedPropertyName, final String operation, final String joinedPropertyName) {
        _selectedPropertyName = selectedPropertyName;
        _operation = operation;
        _joinedPropertyName = joinedPropertyName;
    }

    /**
     * Create a condition selectedProperty &lt;&gt; joinedProperty.
     *
     * @param selectedPropertyName name on from selector
     * @param joinedPropertyName name on join selector
     * @return new join condition
     */
    public static PropertyJoinCondition selectedNotEqualsJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_NOT_EQUALS, joinedPropertyName);
    }

    /**
     * Create a condition selectedProperty &lt; joinedProperty.
     *
     * @param selectedPropertyName name on from selector
     * @param joinedPropertyName name on join selector
     * @return new join condition
     */
    public static PropertyJoinCondition selectedLowerJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_LOWER, joinedPropertyName);
    }

    /**
     * Create a condition selectedProperty &lt;= joinedProperty.
     *
     * @param selectedPropertyName name on from selector
     * @param joinedPropertyName name on join selector
     * @return new join condition
     */
    public static PropertyJoinCondition selectedLowerOrEqualJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_LOWER_EQUAL, joinedPropertyName);
    }

    /**
     * Create a condition selectedProperty = joinedProperty.
     *
     * @param selectedPropertyName name on from selector
     * @param joinedPropertyName name on join selector
     * @return new join condition
     */
    public static PropertyJoinCondition selectedEqualsJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_EQUALS, joinedPropertyName);
    }

    /**
     * Create a condition selectedProperty &gt;= joinedProperty.
     *
     * @param selectedPropertyName name on from selector
     * @param joinedPropertyName name on join selector
     * @return new join condition
     */
    public static PropertyJoinCondition selectedGraterOrEqualJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_GREATER_EQUAL, joinedPropertyName);
    }

    /**
     * Create a condition selectedProperty &gt; joinedProperty.
     *
     * @param selectedPropertyName name on from selector
     * @param joinedPropertyName name on join selector
     * @return new join condition
     */
    public static PropertyJoinCondition selectedGraterJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_GREATER, joinedPropertyName);
    }

    /**
     * Append the join comparison to the SQL2 buffer.
     *
     * @param sql2 buffer to append to
     * @param selectorNames selector names provider
     */
    @Override
    public void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames) {
        sql2.append(selectorNames.getFromSelectorName()).append('.').append(_selectedPropertyName).append(_operation).append(selectorNames.getJoinSelectorName()).append('.').append(_joinedPropertyName);
    }
}
