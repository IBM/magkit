package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * The builder for a sql2 null condition.
 *
 * @author wolf.bubenik@aperto.com
 * @since (18.05.2020)
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

    public static Sql2JoinConstraint isNull(final String propertyName) {
        return new Sql2NullCondition(propertyName, false);
    }

    public static Sql2JoinConstraint isNotNull(final String propertyName) {
        return new Sql2NullCondition(propertyName, true);
    }

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

    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return this;
    }
}
