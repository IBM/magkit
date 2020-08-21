package com.aperto.magkit.query.sql2.condition;

import java.util.Calendar;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Builder class for double property conditions.
 *
 * @author wolf.bubenik@aperto.com
 * @since 08.04.2020
 */
public final class Sql2DoubleCondition extends Sql2PropertyCondition<Sql2DoubleCondition, Double> {

    private Sql2DoubleCondition(final String property) {
        super(property);
    }

    public static Sql2CompareNot<Double> property(final String name) {
        return new Sql2DoubleCondition(name);
    }

    @Override
    protected Sql2DoubleCondition me() {
        return this;
    }

    @Override
    protected void appendValueConstraint(StringBuilder sql2, final String selectorName, String name, Double value) {
        if (value != null) {
            if (isNotBlank(selectorName)) {
                sql2.append(selectorName).append('.');
            }
            sql2.append('[').append(name).append(']').append(getCompareOperator()).append(value);
        }
    }
}