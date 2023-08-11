package com.aperto.magkit.query.sql2.statement;

/**
 * SQL2 statement builder interface for fluent API: Optional ordering step.
 *
 * @author wolf.bubenik@aperto.com
 * @since 15.04.2020
 */
public interface Sql2Order extends Sql2Builder {
    Sql2OrderDirection orderBy(String... attribute);
    Sql2OrderDirection orderByScore();
}
