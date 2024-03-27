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
 * Builder for SQL2 join conditions on path. To be used for joins only.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-18
 */
public final class Sql2PathJoinCondition implements Sql2JoinCondition {

    private final String _method;
    private final boolean _isJoinSelectorFirst;

    private Sql2PathJoinCondition(final String method, final boolean isJoinSelectorFirst) {
        _method = method;
        _isJoinSelectorFirst = isJoinSelectorFirst;
    }

    public static Sql2PathJoinCondition isJoinedDescendantOfSelected() {
        return new Sql2PathJoinCondition(Sql2PathCondition.SQL2_METHOD_DESCENDANT, true);
    }

    public static Sql2PathJoinCondition isSelectedDescendantOfJoined() {
        return new Sql2PathJoinCondition(Sql2PathCondition.SQL2_METHOD_DESCENDANT, false);
    }

    public static Sql2PathJoinCondition isJoinedChildOfSelected() {
        return new Sql2PathJoinCondition(Sql2PathCondition.SQL2_METHOD_CHILD, true);
    }

    public static Sql2PathJoinCondition isSelectedChildOfJoined() {
        return new Sql2PathJoinCondition(Sql2PathCondition.SQL2_METHOD_CHILD, false);
    }

    public static Sql2PathJoinCondition isJoinedEqualsSelected() {
        return new Sql2PathJoinCondition(Sql2PathCondition.SQL2_METHOD_SAME, false);
    }

    @Override
    public void appendTo(StringBuilder sql2, final Sql2SelectorNames selectorNames) {
        String first = _isJoinSelectorFirst ? selectorNames.getJoinSelectorName() : selectorNames.getFromSelectorName();
        String second = _isJoinSelectorFirst ? selectorNames.getFromSelectorName() : selectorNames.getJoinSelectorName();
        sql2.append(_method).append('(').append(first).append(',').append(second).append(')');
    }
}
