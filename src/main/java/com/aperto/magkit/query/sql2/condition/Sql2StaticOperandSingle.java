package com.aperto.magkit.query.sql2.condition;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (20.05.20)
 */
public interface Sql2StaticOperandSingle<V> {
    Sql2JoinConstraint value(V value);
    Sql2JoinConstraint bindVariable(String name);
}
