package com.aperto.magkit.query.sql2.condition;

/**
 * Generic interface for name conditions. Declared methods for the step that provides the value.
 * Allows providing only one value to be used for comparison. Bind variable names are not supported here.
 *
 * @author wolf.bubenik@aperto.com
 * @since (20.05.20)
 */
public interface Sql2NameOperandSingle {
    Sql2JoinConstraint value(String value);
}
