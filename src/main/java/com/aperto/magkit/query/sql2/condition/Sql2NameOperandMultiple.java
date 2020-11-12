package com.aperto.magkit.query.sql2.condition;

/**
 * Generic interface for all property conditions. Declared methods for the step that declares the value.
 * Allows providing one or more values to be used for comparison or a bind variable name.
 *
 * @author wolf.bubenik@aperto.com
 * @since (20.05.20)
 */
public interface Sql2NameOperandMultiple {
    Sql2JoinConstraint values(String... value);
}
