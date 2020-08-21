package com.aperto.magkit.query.sql2.statement;

import com.aperto.magkit.query.sql2.condition.Sql2JoinCondition;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (18.05.2020)
 */
public interface Sql2JoinOn {
    Sql2Where on(Sql2JoinCondition onCondition);
}
