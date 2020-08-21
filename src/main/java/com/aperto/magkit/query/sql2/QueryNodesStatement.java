package com.aperto.magkit.query.sql2;

import com.aperto.magkit.query.sql2.statement.Sql2Builder;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (28.04.20)
 */
public interface QueryNodesStatement<T> {
    public T withStatement(Sql2Builder statementBuilder);
    public T withStatement(final String sql2);
//    Sql2From selectAll();
}
