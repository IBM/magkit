package com.aperto.magkit.query.sql2.query;

import com.aperto.magkit.query.sql2.statement.Sql2Builder;

/**
 * The NodesQueryBuilder step interface declaring methods for the statement.
 *
 * @param <T> the type of Sql2QueryBuilder to be returned by methods
 * @author wolf.bubenik@aperto.com
 * @since (28.04.20)
 */
public interface QueryNodesStatement<T> {
    T withStatement(Sql2Builder statementBuilder);
    T withStatement(String sql2);
}
