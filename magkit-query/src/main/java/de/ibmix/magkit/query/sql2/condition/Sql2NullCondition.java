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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Builder for a SQL2 NULL / NOT NULL property existence condition. Produces fragments like
 * {@code selector.[prop] IS NULL} or {@code selector.[prop] IS NOT NULL} depending on creation method.
 *
 * Thread-safety: Not thread safe.
 * Null handling: Blank property names yield an empty condition (ignored on render).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-18
 */
public final class Sql2NullCondition implements Sql2JoinConstraint {

    private static final String IS = " IS";
    private static final String NOT = " NOT";
    private static final String NULL = " NULL";

    private boolean _forJoin;
    private final boolean _isNot;
    private final String _name;

    private Sql2NullCondition(final String propertyName, final boolean isNot) {
        _name = propertyName;
        _isNot = isNot;
    }

    @Override
    public boolean isNotEmpty() {
        return isNotBlank(_name);
    }

    /**
     * Create an IS NULL condition for the given property.
     *
     * @param propertyName property name
     * @return join-capable condition
     */
    public static Sql2JoinConstraint isNull(final String propertyName) {
        return new Sql2NullCondition(propertyName, false);
    }

    /**
     * Create an IS NOT NULL condition for the given property.
     *
     * @param propertyName property name
     * @return join-capable condition
     */
    public static Sql2JoinConstraint isNotNull(final String propertyName) {
        return new Sql2NullCondition(propertyName, true);
    }

    /**
     * Append the NULL / NOT NULL fragment to the buffer if non-empty.
     * @param sql2 target buffer (never null)
     * @param selectorNames selector name provider (may supply from/join selector names)
     */
    @Override
    public void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames) {
        if (isNotEmpty()) {
            String selectorName = _forJoin ? selectorNames.getJoinSelectorName() : selectorNames.getFromSelectorName();
            if (isNotBlank(selectorName)) {
                sql2.append(selectorName).append('.');
            }
            sql2.append('[').append(_name).append(']').append(IS);
            if (_isNot) {
                sql2.append(NOT);
            }
            sql2.append(NULL);
        }
    }

    /**
     * Switch to using the join selector name during rendering.
     * @return this for fluent chaining
     */
    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return this;
    }
}
