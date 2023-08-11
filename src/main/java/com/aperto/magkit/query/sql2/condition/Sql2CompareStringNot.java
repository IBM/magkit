package com.aperto.magkit.query.sql2.condition;

/**
 * The String interface for property constraints.
 * Including not(). To be used when not() has not been called.
 *
 * @author wolf.bubenik@aperto.com
 * @since (26.05.20)
 */
public interface Sql2CompareStringNot extends Sql2CompareString {
    Sql2CompareString not();
}
