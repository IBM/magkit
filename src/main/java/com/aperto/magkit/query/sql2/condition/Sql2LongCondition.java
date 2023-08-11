package com.aperto.magkit.query.sql2.condition;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Builder class for long property conditions.
 *
 * @author wolf.bubenik@aperto.com
 * @since 08.04.2020
 */
public final class Sql2LongCondition extends Sql2PropertyCondition<Sql2LongCondition, Long> {

    private Sql2LongCondition(final String property) {
        super(property);
    }

    public static Sql2CompareNot<Long> property(final String name) {
        return new Sql2LongCondition(name);
    }

    @Override
    protected Sql2LongCondition me() {
        return this;
    }

    @Override
    protected void appendValueConstraint(StringBuilder sql2, final String selectorName, String name, Long value) {
        if (value != null) {
            if (isNotBlank(selectorName)) {
                sql2.append(selectorName).append('.');
            }
            sql2.append('[').append(name).append(']').append(getCompareOperator()).append(value);
        }
    }
}
