package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Base condition builder class for all value types.
 *
 * @param <T> the implementing type of this
 * @param <V> the type of the property (String, Long, Double, Calendar)
 * @author wolf.bubenik@aperto.com
 * @since 02.04.2020
 **/
public abstract class Sql2PropertyCondition<T extends Sql2PropertyCondition<T, V>, V> implements Sql2CompareNot<V>, Sql2StaticOperandSingle<V>, Sql2StaticOperandMultiple<V>, Sql2JoinConstraint {

    public static final String METHOD_NOT = "not";
    public static final String METHOD_NAME = "name";

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

    @Override
    public boolean isNotEmpty() {
        return isNotBlank(_name) && (_hasValues || _hasBindVariable);
    }

    public Sql2Compare<V> not() {
        _not = true;
        return me();
    }

    public final Sql2StaticOperandSingle<V> lowerThan() {
        _compareOperator = SQL2_OP_LOWER;
        return me();
    }

    public final Sql2StaticOperandSingle<V> lowerOrEqualThan() {
        _compareOperator = SQL2_OP_LOWER_EQUAL;
        return me();
    }

    public final Sql2StaticOperandMultiple<V> equalsAny() {
        _compareOperator = SQL2_OP_EQUALS;
        return me();
    }

    public final Sql2StaticOperandMultiple<V> equalsAll() {
        _compareOperator = SQL2_OP_EQUALS;
        _joinOperator = SQL2_OP_AND;
        return me();
    }

    public Sql2StaticOperandSingle<V> greaterOrEqualThan() {
        _compareOperator = SQL2_OP_GREATER_EQUAL;
        return me();
    }

    public final Sql2StaticOperandSingle<V> greaterThan() {
        _compareOperator = SQL2_OP_GREATER;
        return me();
    }

    public final Sql2StaticOperandMultiple<V> excludeAny() {
        _compareOperator = SQL2_OP_NOT_EQUALS;
        return me();
    }

    public final Sql2StaticOperandMultiple<V> excludeAll() {
        _compareOperator = SQL2_OP_NOT_EQUALS;
        _joinOperator = SQL2_OP_AND;
        return me();
    }

    @SafeVarargs
    public final Sql2JoinConstraint values(V... values) {
        withValues(values);
        return me();
    }

    public Sql2JoinConstraint value(V value) {
        withValues(value);
        return me();
    }

    public Sql2JoinConstraint bindVariable(String name) {
        _bindVariableName = name;
        _hasBindVariable = isNotBlank(name);
        return me();
    }

    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return me();
    }

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
                if (isNotBlank(selectorName)) {
                    sql2.append(selectorName).append('.');
                }
                sql2.append('[').append(_name).append(']').append(getCompareOperator()).append('$').append(_bindVariableName);
            } else {
                String operator = EMPTY;
                for (V value : _values) {
                    sql2.append(operator);
                    appendValueConstraint(sql2, selectorName, _name, value);
                    operator = _joinOperator;
                }
            }

            if (_not || _isMultiValue) {
                sql2.append(')');
            }
        }
    }

    @SafeVarargs
    final void withValues(final V... values) {
        _values = values;
        _hasValues = values != null && values.length > 0;
        _isMultiValue = _hasValues && values.length > 1;
    }

    void withOperator(final String joinOperator) {
        _joinOperator = joinOperator;
    }

    String getCompareOperator() {
        return _compareOperator;
    }

    abstract T me();

    abstract void appendValueConstraint(final StringBuilder sql2, final String selectorName, final String name, final V value);
}
