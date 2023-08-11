package com.aperto.magkit.query.sql2.condition;

/**
 * The generic interface for property constraints.
 * Including not(). To be used when not() has not been called.
 *
 * @param <V> the type of the property (String, Long, Double, Calendar)
 * @author wolf.bubenik@aperto.com
 * @since 07.04.2020
 */
public interface Sql2CompareNot<V> extends Sql2Compare<V> {
    Sql2Compare<V> not();
}
