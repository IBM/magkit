package com.aperto.magkit.query.sql2.condition;

/**
 * Builder Interface for String conditions that declares usage of jcr methods for strings.
 *
 * @author wolf.bubenik@aperto.com
 * @since (20.05.2020)
 */
public interface Sql2NameOperand extends Sql2NameCompare {
    Sql2NameCompare lowerCase();
    Sql2NameCompare upperCase();
}
