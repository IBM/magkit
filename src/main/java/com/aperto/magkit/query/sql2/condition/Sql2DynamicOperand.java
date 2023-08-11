package com.aperto.magkit.query.sql2.condition;

/**
 * Builder Interface for String conditions that declares usage of jcr methods for strings.
 *
 * @author wolf.bubenik@aperto.com
 * @since (20.05.2020)
 */
public interface Sql2DynamicOperand extends Sql2CompareStringNot {
    Sql2CompareStringNot lowerCase();
    Sql2CompareStringNot upperCase();
    Sql2CompareNot<Long> length();
}
