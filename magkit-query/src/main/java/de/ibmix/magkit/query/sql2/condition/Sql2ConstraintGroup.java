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

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Builder for grouping several {@link Sql2JoinConstraint} instances with a logical operator (AND/OR) and
 * optional negation (NOT). Empty child constraints are ignored so callers can compose groups without
 * cumbersome null / emptiness checks.
 * <p>
 * Features:
 * <ul>
 *   <li>AND / OR logical grouping via {@link #and()} / {@link #or()}</li>
 *   <li>Optional NOT wrapper via {@link #not()}</li>
 *   <li>Transparent propagation of join selector usage via {@link #forJoin()}</li>
 *   <li>Skips empty constraints to keep output clean</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>{@code
 * String fragment = Sql2ConstraintGroup.and()
 *     .matches(
 *         Sql2StringCondition.property("title").equalsAny().values("Hello"),
 *         Sql2LongCondition.property("views").greaterThan().value(100L)
 *     )
 *     .asString();
 * // -> "([title] = 'Hello' AND [views] > 100)"
 * }</pre>
 * Thread-safety: Not thread safe.
 * Null handling: Null/empty arrays in {@link #matches(Sql2JoinConstraint...)} yield an empty group.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-02-28
 */
public final class Sql2ConstraintGroup implements Sql2JoinConstraint {

    private final String _operator;
    private boolean _not;
    private Sql2JoinConstraint[] _constraints;
    private boolean _hasConstraints;
    private boolean _forJoin;

    private Sql2ConstraintGroup(final String operator) {
        _operator = operator;
    }

    /**
     * Create a group that joins child constraints with logical AND.
     *
     * @return new group instance
     */
    public static Sql2ConstraintGroup and() {
        return new Sql2ConstraintGroup(SQL2_OP_AND);
    }

    /**
     * Create a group that joins child constraints with logical OR.
     *
     * @return new group instance
     */
    public static Sql2ConstraintGroup or() {
        return new Sql2ConstraintGroup(SQL2_OP_OR);
    }

    /**
     * Indicates whether at least one child constraint reference was supplied (not considering individual emptiness).
     * @return true if group may render output, false otherwise
     */
    @Override
    public boolean isNotEmpty() {
        return _hasConstraints;
    }

    /**
     * Negate the constraint group (wrap output with {@code not(...)}).
     *
     * @return this for fluent chaining
     */
    public Sql2ConstraintGroup not() {
        _not = true;
        return this;
    }

    /**
     * Provide the child constraints to be grouped. Empty or null constraints are ignored at render time.
     *
     * @param conditions constraints to group (may be null / empty)
     * @return this for fluent chaining
     */
    public Sql2ConstraintGroup matches(final Sql2JoinConstraint... conditions) {
        _constraints = conditions;
        // TODO: If all conditions are empty, _hasConstraints should be false.
        _hasConstraints = conditions != null && conditions.length > 0;
        return this;
    }

    /**
     * Propagate join selector usage to all child constraints during rendering.
     * @return this for fluent chaining
     */
    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return this;
    }

    /**
     * Append the grouped constraint fragment honoring logical operator and optional NOT wrapper.
     * @param sql2 target buffer
     * @param selectorNames selector name provider
     */
    @Override
    public void appendTo(final StringBuilder sql2, final Sql2SelectorNames selectorNames) {
        if (_hasConstraints) {
            if (_not) {
                sql2.append("not(");
            } else if (_constraints.length > 1) {
                sql2.append('(');
            }
            appendConstraints(sql2, selectorNames);
            if (_not || _constraints.length > 1) {
                sql2.append(')');
            }
        }
    }

    private void appendConstraints(final StringBuilder sql2, final Sql2SelectorNames selectorNames) {
        String operation = EMPTY;
        for (Sql2JoinConstraint c : _constraints) {
            if (c.isNotEmpty()) {
                if (_forJoin) {
                    c.forJoin();
                }
                sql2.append(operation);
                c.appendTo(sql2, selectorNames);
                operation = _operator;
            }
        }
    }
}
