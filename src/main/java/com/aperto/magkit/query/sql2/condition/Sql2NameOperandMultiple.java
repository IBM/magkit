package com.aperto.magkit.query.sql2.condition;

/**
 * Generic interface name conditions. Declares methods for the step that provides the values.
 * Allows providing one or more values to be used for comparison. Bind variable names are not supported here.
 *
 * @author wolf.bubenik@aperto.com
 * @since (11.11.2020)
 */
public interface Sql2NameOperandMultiple {
    Sql2JoinConstraint values(String... value);
}
