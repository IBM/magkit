package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;

/**
 * The builder for a sql2 name condition.
 * Not yet implemented!
 *
 * @author wolf.bubenik@aperto.com
 * @since (26.05.2020)
 */
public final class Sql2NameCondition extends Sql2StringCondition {

    public static final String METHOD_NAME = "name";

    private final String _method;

    private Sql2NameCondition(String method) {
        super(null);
        _method = method;
    }

    public static Sql2NameCondition name() {
        return new Sql2NameCondition(METHOD_NAME);
    }

    @Override
    public void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames) {
        // sorry, not jet implemented
        throw new UnsupportedOperationException();
    }
}
