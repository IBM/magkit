package com.aperto.magkit.query.sql2.condition;

/**
 * Builder Interface for String conditions that declares methods only available for Strings.
 *
 * @author wolf.bubenik@aperto.com
 * @since (26.05.20)
 */
public interface Sql2CompareString extends Sql2Compare<String> {
    Sql2StaticOperandMultiple<String> startsWithAny();
    Sql2StaticOperandMultiple<String> endsWithAny();
    Sql2StaticOperandMultiple<String> likeAny();
    Sql2StaticOperandMultiple<String> likeAll();
}
