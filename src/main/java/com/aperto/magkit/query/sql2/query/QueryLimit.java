package com.aperto.magkit.query.sql2.query;

/**
 * The QueryBuilder step interface declaring methods for the query limits and offsets.
 *
 * @param <T> the type of Sql2QueryBuilder to be returned by methods
 * @author wolf.bubenik@aperto.com
 * @since (28.04.20)
 */
public interface QueryLimit<T>  {
    T withLimit(long limit);
    T withOffset(long offset);
}
