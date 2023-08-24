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
 * The builder for a sql2 condition group.
 *
 * @author wolf.bubenik@aperto.com
 * @since 28.02.2020
 **/
public final class Sql2ConstraintGroup implements Sql2JoinConstraint {

    private final String _operator;
    private boolean _not;
    private Sql2JoinConstraint[] _constraints;
    private boolean _hasConstraints;
    private boolean _forJoin;

    private Sql2ConstraintGroup(final String operator) {
        _operator = operator;
    }

    public static Sql2ConstraintGroup and() {
        return new Sql2ConstraintGroup(SQL2_OP_AND);
    }

    public static Sql2ConstraintGroup or() {
        return new Sql2ConstraintGroup(SQL2_OP_OR);
    }

    @Override
    public boolean isNotEmpty() {
        return _hasConstraints;
    }

    public Sql2ConstraintGroup not() {
        _not = true;
        return this;
    }

    public Sql2ConstraintGroup matches(final Sql2JoinConstraint... conditions) {
        _constraints = conditions;
        // TODO: If all conditions are empty, _hasConstraints should be false.
        _hasConstraints = conditions != null && conditions.length > 0;
        return this;
    }

    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return this;
    }

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
