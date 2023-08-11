package com.aperto.magkit.query.sql2.condition;

/**
 * Generic interface for all property conditions. Declared methods for the step that declares the value.
 * Allows providing only one value to be used for comparison or a bind variable name.
 *
 * @param <V> the type of the value
 * @author wolf.bubenik@aperto.com
 * @since (20.05.20)
 */
public interface Sql2StaticOperandSingle<V> {
    Sql2JoinConstraint value(V value);
    Sql2JoinConstraint bindVariable(String name);
}
