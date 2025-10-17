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
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Builder for a SQL2 node name condition using JCR function name(). Limited compared to string property
 * conditions: no LIKE, no length(), restricted NOT usage by repository constraints. Allows case-transform
 * helpers and comparison operators. Supports single or multiple value comparisons with OR / AND semantics
 * (depending on operator) and exclusion via not equals.
 * <p>Usage example:</p>
 * <pre>{@code
 * String fragment = new Sql2NameCondition()
 *     .upperCase()
 *     .equalsAny()
 *     .values("Home", "About")
 *     .asString("a", null);
 * // -> "(upper(name(a)) = 'Home' OR upper(name(a)) = 'About')"
 * }</pre>
 * Thread-safety: Not thread safe.
 * Null handling: Empty/blank values are ignored resulting in an empty condition.
 * Side effects: Mutating builder; each method updates internal state.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-11-11
 */
public class Sql2NameCondition implements Sql2NameOperand, Sql2NameCompare, Sql2NameOperandSingle, Sql2NameOperandMultiple, Sql2JoinConstraint {

    public static final String METHOD_NAME = "name";

    private boolean _hasValues;
    private String[] _values;
    private String _joinOperator = SQL2_OP_OR;
    private boolean _isMultiValue;
    private String _compareOperator;
    private boolean _forJoin;
    private String _operandMethod;

    /**
     * Apply a lower() case transformation to the name() operand before comparison.
     * @return comparison API for further operator selection
     */
    public Sql2NameCompare lowerCase() {
        _operandMethod = Sql2StringCondition.METHOD_LOWER;
        return me();
    }

    /**
     * Apply an upper() case transformation to the name() operand before comparison.
     * @return comparison API for further operator selection
     */
    public Sql2NameCompare upperCase() {
        _operandMethod = Sql2StringCondition.METHOD_UPPER;
        return me();
    }

    /**
     * Mark this condition to use the join selector name when rendering (instead of the FROM selector).
     * @return this for fluent chaining
     */
    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return me();
    }

    /**
     * Indicates whether at least one non-empty value was provided.
     * @return true if condition will render output
     */
    @Override
    public boolean isNotEmpty() {
        return _hasValues;
    }

    /**
     * Start a strictly lower-than comparison on the transformed or raw name() value.
     * @return single-value step
     */
    @Override
    public Sql2NameOperandSingle lowerThan() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_LOWER;
        return me();
    }

    /**
     * Start a lower-or-equal-than comparison on the name() value.
     * @return single-value step
     */
    @Override
    public Sql2NameOperandSingle lowerOrEqualThan() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_LOWER_EQUAL;
        return me();
    }

    /**
     * Expect the name() value to equal ANY of the provided values (OR semantics for multi-value).
     * @return multi-value step
     */
    @Override
    public Sql2NameOperandMultiple equalsAny() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_EQUALS;
        return me();
    }

    /**
     * Start a greater-or-equal-than comparison.
     * @return single-value step
     */
    @Override
    public Sql2NameOperandSingle greaterOrEqualThan() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_GREATER_EQUAL;
        return me();
    }

    /**
     * Start a strictly greater-than comparison.
     * @return single-value step
     */
    @Override
    public Sql2NameOperandSingle greaterThan() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_GREATER;
        return me();
    }

    /**
     * Exclude ANY of the provided values (name() <> value OR ... for multi-value).
     * @return multi-value step
     */
    @Override
    public Sql2NameOperandMultiple excludeAny() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_NOT_EQUALS;
        return me();
    }

    /**
     * Provide one or more node name values to compare with (multi value variant).
     * @param values one or more node names (null/blank ignored in rendering)
     * @return next step allowing join selector decision
     */
    @Override
    public Sql2JoinConstraint values(String... values) {
        withValues(values);
        return me();
    }

    /**
     * Provide a single node name value to compare with.
     * @param value node name (may be null)
     * @return next step allowing join selector decision
     */
    @Override
    public Sql2JoinConstraint value(String value) {
        withValues(value);
        return me();
    }

    private void withValues(final String... values) {
        _values = values;
        _hasValues = values != null && values.length > 0 && _values[0] != null;
        _isMultiValue = _hasValues && values.length > 1 && _values[1] != null;
    }

    /**
     * Append the rendered name() comparison (including case transformation) to the buffer.
     * Empty or blank values are skipped entirely.
     * @param sql2 target buffer (never null)
     * @param selectorNames selector name provider (may be null -> no selector prefix)
     */
    @Override
    public void appendTo(final StringBuilder sql2, final Sql2SelectorNames selectorNames) {
        if (isNotEmpty()) {
            if (_isMultiValue) {
                sql2.append('(');
            }

            String selectorName = _forJoin ? selectorNames.getJoinSelectorName() : selectorNames.getFromSelectorName();
            if (_isMultiValue) {
                appendValues(sql2, selectorName);
            } else {
                appendValueConstraint(sql2, selectorName, _values[0]);
            }

            if (_isMultiValue) {
                sql2.append(')');
            }
        }
    }

    private void appendValues(final StringBuilder sql2, final String selectorName) {
        String operator = EMPTY;
        for (String value : _values) {
            sql2.append(operator);
            appendValueConstraint(sql2, selectorName, value);
            operator = _joinOperator;
        }
    }

    private void appendValueConstraint(final StringBuilder sql2, final String selectorName, final String value) {
        if (StringUtils.isNotEmpty(value)) {
            final String cleanValue = value.replaceAll("'", "''");

            if (StringUtils.isNotEmpty(_operandMethod)) {
                sql2.append(_operandMethod).append('(');
            }

            sql2.append(METHOD_NAME).append('(').append(StringUtils.trimToEmpty(selectorName)).append(')');

            if (StringUtils.isNotEmpty(_operandMethod)) {
                sql2.append(')');
            }

            sql2.append(_compareOperator).append('\'').append(cleanValue).append('\'');
        }
    }

    /**
     * Self reference to keep fluent API strongly typed.
     * @return this builder instance
     */
    public Sql2NameCondition me() {
        return this;
    }
}
