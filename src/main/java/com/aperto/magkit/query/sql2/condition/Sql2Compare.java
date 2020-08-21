package com.aperto.magkit.query.sql2.condition;

/**
 * The generic interface for property constraints.
 * Excluding not(). To be used when not() has been called.
 *
 * @param <V> the type of the property (String, Long, Double, Calendar)
 * @author wolf.bubenik@aperto.com
 * @since 07.04.2020
 */
public interface Sql2Compare<V> {
    Sql2StaticOperandSingle<V> lowerThan();
    Sql2StaticOperandSingle<V> lowerOrEqualThan();
    Sql2StaticOperandMultiple<V> equalsAny();
    Sql2StaticOperandMultiple<V> equalsAll();
    Sql2StaticOperandSingle<V> greaterOrEqualThan();
    Sql2StaticOperandSingle<V> greaterThan();
    Sql2StaticOperandMultiple<V> excludeAny();
    Sql2StaticOperandMultiple<V> excludeAll();
}
