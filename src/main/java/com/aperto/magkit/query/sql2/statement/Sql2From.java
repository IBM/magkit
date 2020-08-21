package com.aperto.magkit.query.sql2.statement;

/**
 * SQL2 statement builder interface for fluent API: Optional from step.
 *
 * @author wolf.bubenik@aperto.com
 * @since 15.04.2020
 */
public interface Sql2From extends Sql2As {
    Sql2As from(String nodeType);
}
