package com.aperto.magkit.dialogs.m6.fields;

/**
 * Interface for common specific multi field definitions.
 *
 * @author frank.sommer
 * @since 05.03.2021
 */
public interface SpecificMultiDefinition {
    long DEFAULT_MAX = 3;

    Long getMaxComponents();

    String getParentComponentProperty();
}
