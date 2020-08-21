package com.aperto.magkit.query.sql2.statement;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (15.05.2020)
 */
public interface Sql2Join extends Sql2Where {
    Sql2JoinAs innerJoin(String nodeType);
    Sql2JoinAs leftOuterJoin(String nodeType);
    Sql2JoinAs rightOuterJoin(String nodeType);
}
