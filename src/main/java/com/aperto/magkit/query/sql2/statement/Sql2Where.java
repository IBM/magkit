package com.aperto.magkit.query.sql2.statement;

import com.aperto.magkit.query.sql2.condition.Sql2JoinConstraint;

/**
 * SQL2 statement builder interface for fluent API: Optional where step.
 *
 * @author wolf.bubenik@aperto.com
 * @since 15.04.2020
 */
public interface Sql2Where extends Sql2Order {
    Sql2Order whereAll(Sql2JoinConstraint... constraints);
    Sql2Order whereAny(Sql2JoinConstraint... constraints);
}
