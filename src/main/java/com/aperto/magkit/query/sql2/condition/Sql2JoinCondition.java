package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;

/**
 * The base interface for join conditions.
 *
 * @author wolf.bubenik@aperto.com
 * @since (18.05.2020)
 */
public interface Sql2JoinCondition {
    void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames);
}
