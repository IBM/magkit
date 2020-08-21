package com.aperto.magkit.query.sql2.condition;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (20.05.20)
 */
public interface Sql2StaticOperandMultiple<V> {
    Sql2JoinConstraint values(V... value);
    Sql2JoinConstraint bindVariable(String name);
}
