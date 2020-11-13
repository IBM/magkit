package com.aperto.magkit.query.sql2.condition;

/**
 * The generic interface for property constraints.
 * Excluding not(). To be used when not() has been called.
 *
 * @author wolf.bubenik@aperto.com
 * @since 11.11.2020
 */
public interface Sql2NameCompare {
    Sql2NameOperandSingle lowerThan();
    Sql2NameOperandSingle lowerOrEqualThan();
    Sql2NameOperandMultiple equalsAny();
    Sql2NameOperandSingle greaterOrEqualThan();
    Sql2NameOperandSingle greaterThan();
    Sql2NameOperandMultiple excludeAny();
}
