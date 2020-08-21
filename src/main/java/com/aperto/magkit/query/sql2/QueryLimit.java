package com.aperto.magkit.query.sql2;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (28.04.20)
 */
public interface QueryLimit<T>  {
    T withLimit(long limit);
    T withOffset(long offset);
}
