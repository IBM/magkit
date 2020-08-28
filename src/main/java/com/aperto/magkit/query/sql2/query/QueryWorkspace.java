package com.aperto.magkit.query.sql2.query;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (27.04.20)
 */
public interface QueryWorkspace<T> {
    T fromWorkspace(String workspace);
    T fromWebsite();
}
