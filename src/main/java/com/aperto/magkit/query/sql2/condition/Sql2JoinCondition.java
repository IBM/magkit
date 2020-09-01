package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (18.05.2020)
 */
public interface Sql2JoinCondition {
    void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames);
}
