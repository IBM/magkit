package com.aperto.magkit.query.sql2.statement;

/**
 * The interface for the last step of query statement building: Build the query string.
 *
 * @author wolf.bubenik@aperto.com
 * @since 28.02.2020
 */
public interface Sql2Builder {
    String build();
}
