package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;

/**
 * Base builder interface for all constraints.
 *
 * @author wolf.bubenik@aperto.com
 * @since 28.02.20
 **/
public interface Sql2Constraint {
    String SQL2_OP_OR = " OR ";
    String SQL2_OP_AND = " AND ";

    void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames);

    boolean isNotEmpty();

    default String asString() {
        StringBuilder result = new StringBuilder();
        appendTo(result, new Sql2SelectorNames() {
            @Override
            public String getFromSelectorName() {
                return null;
            }

            @Override
            public String getJoinSelectorName() {
                return null;
            }
        });
        return result.toString();
    }
}
