package com.aperto.magkit.query.sql2.condition;

/**
 * Base builder interface for join conditions.
 *
 * @author wolf.bubenik@aperto.com
 * @since (18.06.2020)
 */
public interface Sql2JoinConstraint extends Sql2Constraint {
    Sql2JoinConstraint forJoin();
}
