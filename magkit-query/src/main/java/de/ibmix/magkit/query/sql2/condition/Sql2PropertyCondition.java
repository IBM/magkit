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
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Base condition builder class for all value types providing comparison operator selection (including NOT),
 * multi-value handling with OR/AND semantics and optional bind variable support. Subclasses are responsible
 * for rendering the literal value representation via {@link #appendValueConstraint(StringBuilder, String, String, Object)}.
 *
 * Features:
 * <ul>
 *   <li>NOT support (single application only) via {@link #not()}</li>
 *   <li>AND/OR combination for equals/excludes via {@link #equalsAll()} / {@link #excludeAll()}</li>
 *   <li>Bind variable support ({@link #bindVariable(String)}) with automatic $ prefix insertion</li>
 *   <li>Selector aware output for joins via {@link #forJoin()}</li>
 * </ul>
 * Thread-safety: Not thread safe.
 * Null handling: Null property name or missing values produce an empty condition. Null values in an array
 * truncate multi-value handling after the first null occurrence.
 * @param <T> the implementing type of this
 * @param <V> the type of the property (String, Long, Double, Calendar)
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-02
 **/
public abstract class Sql2PropertyCondition<T extends Sql2PropertyCondition<T, V>, V> implements Sql2CompareNot<V>, Sql2StaticOperandSingle<V>, Sql2StaticOperandMultiple<V>, Sql2JoinConstraint {

    public static final String METHOD_NOT = "not";

    public static final String SQL2_OP_EQUALS = " = ";
    public static final String SQL2_OP_NOT_EQUALS = " <> ";
    public static final String SQL2_OP_LOWER = " < ";
    public static final String SQL2_OP_LOWER_EQUAL = " <= ";
    public static final String SQL2_OP_GREATER_EQUAL = " >= ";
    public static final String SQL2_OP_GREATER = " > ";

    private final String _name;
    private V[] _values;
    private String _joinOperator = SQL2_OP_OR;
    private boolean _not;
    private boolean _hasValues;
    private boolean _hasBindVariable;
    private boolean _isMultiValue;
    private String _compareOperator;
    private boolean _forJoin;
    private String _bindVariableName;

    protected Sql2PropertyCondition(final String name) {
        _name = name;
    }

    /**
     * Indicates whether the condition holds effective state (values or bind variable) and a non-blank property name.
     * @return true when rendering would produce output, false otherwise
     */
    @Override
    public boolean isNotEmpty() {
        return isNotBlank(_name) && (_hasValues || _hasBindVariable);
    }

    /**
     * Negate the upcoming comparison (logical NOT). Only one NOT application is allowed per instance.
     * @return comparison API without another not() method
     */
    public Sql2Compare<V> not() {
        _not = true;
        return me();
    }

    /**
     * Start a strictly lower-than comparison.
     * @return single-value operand step
     */
    public final Sql2StaticOperandSingle<V> lowerThan() {
        _compareOperator = SQL2_OP_LOWER;
        return me();
    }

    /**
     * Start a lower-or-equal-than comparison.
     * @return single-value operand step
     */
    public final Sql2StaticOperandSingle<V> lowerOrEqualThan() {
        _compareOperator = SQL2_OP_LOWER_EQUAL;
        return me();
    }

    /**
     * Expect ANY of the provided values (OR semantics) using equals operator.
     * @return multi-value operand step
     */
    public final Sql2StaticOperandMultiple<V> equalsAny() {
        _compareOperator = SQL2_OP_EQUALS;
        return me();
    }

    /**
     * Expect ALL provided values (AND semantics) using equals operator.
     * @return multi-value operand step
     */
    public final Sql2StaticOperandMultiple<V> equalsAll() {
        _compareOperator = SQL2_OP_EQUALS;
        _joinOperator = SQL2_OP_AND;
        return me();
    }

    /**
     * Start a greater-or-equal-than comparison.
     * @return single-value operand step
     */
    public Sql2StaticOperandSingle<V> greaterOrEqualThan() {
        _compareOperator = SQL2_OP_GREATER_EQUAL;
        return me();
    }

    /**
     * Start a strictly greater-than comparison.
     * @return single-value operand step
     */
    public final Sql2StaticOperandSingle<V> greaterThan() {
        _compareOperator = SQL2_OP_GREATER;
        return me();
    }

    /**
     * Exclude ANY of the provided values (OR semantics) using not equals.
     * @return multi-value operand step
     */
    public final Sql2StaticOperandMultiple<V> excludeAny() {
        _compareOperator = SQL2_OP_NOT_EQUALS;
        return me();
    }

    /**
     * Exclude ALL provided values (AND semantics) using not equals.
     * @return multi-value operand step
     */
    public final Sql2StaticOperandMultiple<V> excludeAll() {
        _compareOperator = SQL2_OP_NOT_EQUALS;
        _joinOperator = SQL2_OP_AND;
        return me();
    }

    /**
     * Provide one or more literal values for comparison. Null values stop multi-value handling at first null position.
     * @param values values to compare with (may be null / empty)
     * @return next step allowing join selector decision
     */
    @SafeVarargs
    public final Sql2JoinConstraint values(V... values) {
        withValues(values);
        return me();
    }

    /**
     * Provide a single literal value for comparison.
     * @param value value to compare (may be null)
     * @return next step allowing join selector decision
     */
    public Sql2JoinConstraint value(V value) {
        withValues(value);
        return me();
    }

    /**
     * Use a bind variable instead of literal values. Leading $ is added if missing.
     * @param name bind variable name (trimmed) – may be null/blank (ignored)
     * @return next step allowing join selector decision
     */
    public Sql2JoinConstraint bindVariable(String name) {
        _bindVariableName = trim(name);
        _hasBindVariable = isNotBlank(_bindVariableName);
        return me();
    }

    /**
     * Render using the join selector name instead of the from selector.
     * @return this for fluent chaining
     */
    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return me();
    }

    /**
     * Append this property condition to the buffer if non-empty. Handles NOT, multi-value parenthesis and bind variables.
     * @param sql2 accumulating SQL2 buffer (never null)
     * @param selectorNames selector name provider
     */
    @Override
    public void appendTo(StringBuilder sql2, final Sql2SelectorNames selectorNames) {
        if (isNotEmpty()) {
            if (_not) {
                sql2.append(METHOD_NOT);
            }
            if (_isMultiValue || _not) {
                sql2.append('(');
            }

            String selectorName = _forJoin ? selectorNames.getJoinSelectorName() : selectorNames.getFromSelectorName();
            if (_hasBindVariable) {
                appendBindVariable(sql2, selectorName);
            } else if (_isMultiValue) {
                appendValues(sql2, selectorName);
            } else {
                appendValueConstraint(sql2, selectorName, _name, _values[0]);
            }

            if (_not || _isMultiValue) {
                sql2.append(')');
            }
        }
    }

    private void appendValues(final StringBuilder sql2, final String selectorName) {
        String operator = EMPTY;
        for (V value : _values) {
            sql2.append(operator);
            appendValueConstraint(sql2, selectorName, _name, value);
            operator = _joinOperator;
        }
    }

    private void appendBindVariable(final StringBuilder sql2, final String selectorName) {
        if (isNotBlank(selectorName)) {
            sql2.append(selectorName).append('.');
        }
        sql2.append('[').append(_name).append(']').append(getCompareOperator());
        if (_bindVariableName.charAt(0) != '$') {
            sql2.append('$');
        }
        sql2.append(_bindVariableName);
    }

    @SafeVarargs
    final void withValues(final V... values) {
        _values = values;
        _hasValues = values != null && values.length > 0 && _values[0] != null;
        _isMultiValue = _hasValues && values.length > 1 && _values[1] != null;
    }

    void withOperator(final String joinOperator) {
        _joinOperator = joinOperator;
    }

    String getCompareOperator() {
        return _compareOperator;
    }

    /**
     * Self reference for fluent API (covariant typing).
     * @return this instance cast to implementing type
     */
    abstract T me();

    /**
     * Append a single literal value constraint (implemented by subclasses to adapt literal formatting).
     * @param sql2 buffer
     * @param selectorName selector name or null
     * @param name property name
     * @param value literal value
     */
    abstract void appendValueConstraint(StringBuilder sql2, String selectorName, String name, V value);
}
