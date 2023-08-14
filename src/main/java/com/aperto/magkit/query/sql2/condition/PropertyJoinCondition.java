package com.aperto.magkit.query.sql2.condition;

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

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (18.05.2020)
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

    public PropertyJoinCondition selectedNotEqualsJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_NOT_EQUALS, joinedPropertyName);
    }

    public PropertyJoinCondition selectedLowerJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_LOWER, joinedPropertyName);
    }

    public PropertyJoinCondition selectedLowerOrEqualJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_LOWER_EQUAL, joinedPropertyName);
    }

    public PropertyJoinCondition selectedEqualsJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_EQUALS, joinedPropertyName);
    }

    public PropertyJoinCondition selectedGraterOrEqualJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_GREATER_EQUAL, joinedPropertyName);
    }

    public PropertyJoinCondition selectedGraterJoined(final String selectedPropertyName, final String joinedPropertyName) {
        return new PropertyJoinCondition(selectedPropertyName, Sql2PropertyCondition.SQL2_OP_GREATER, joinedPropertyName);
    }

    @Override
    public void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames) {
        sql2.append(selectorNames.getFromSelectorName()).append('.').append(_selectedPropertyName).append(_operation).append(selectorNames.getJoinSelectorName()).append('.').append(_joinedPropertyName);
    }
}
