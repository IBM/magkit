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
 * The builder for a sql2 node name condition.
 * Note that it is very similar to a StringCondition but supports less operations.
 * Jackrabbit does not allow to combine name() with not(), length() and LIKE.
 *
 * @author wolf.bubenik@aperto.com
 * @since (11.11.2020)
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

    public Sql2NameCompare lowerCase() {
        _operandMethod = Sql2StringCondition.METHOD_LOWER;
        return me();
    }

    public Sql2NameCompare upperCase() {
        _operandMethod = Sql2StringCondition.METHOD_UPPER;
        return me();
    }

    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return me();
    }

    @Override
    public boolean isNotEmpty() {
        return _hasValues;
    }

    @Override
    public Sql2NameOperandSingle lowerThan() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_LOWER;
        return me();
    }

    @Override
    public Sql2NameOperandSingle lowerOrEqualThan() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_LOWER_EQUAL;
        return me();
    }

    @Override
    public Sql2NameOperandMultiple equalsAny() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_EQUALS;
        return me();
    }

    @Override
    public Sql2NameOperandSingle greaterOrEqualThan() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_GREATER_EQUAL;
        return me();
    }

    @Override
    public Sql2NameOperandSingle greaterThan() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_GREATER;
        return me();
    }

    @Override
    public Sql2NameOperandMultiple excludeAny() {
        _compareOperator = Sql2PropertyCondition.SQL2_OP_NOT_EQUALS;
        return me();
    }

    @Override
    public Sql2JoinConstraint values(String... values) {
        withValues(values);
        return me();
    }

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

    public Sql2NameCondition me() {
        return this;
    }
}
