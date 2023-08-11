package com.aperto.magkit.query.sql2.statement;

/**
 * SQL2 statement builder interface for fluent API: Optional order direction step.
 *
 * @author wolf.bubenik@aperto.com
 * @since 15.04.2020
 */
public interface Sql2OrderDirection extends Sql2Builder {
    Sql2Builder descending();
    Sql2Builder ascending();
}
