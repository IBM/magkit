package com.aperto.magkit.query.sql2.query;

/**
 * Query Builder interface for the workspace step.
 *
 * @param <T> the type of Sql2QueryBuilder to be returned by methods
 * @author wolf.bubenik@aperto.com
 * @since (27.04.20)
 */
public interface QueryWorkspace<T> {
    T fromWorkspace(String workspace);
    T fromWebsite();
}
