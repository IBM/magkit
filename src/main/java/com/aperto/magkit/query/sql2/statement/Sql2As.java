package com.aperto.magkit.query.sql2.statement;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (15.05.20)
 */
public interface Sql2As extends Sql2Where {
    Sql2Join selectAs(String selectorName);
}
