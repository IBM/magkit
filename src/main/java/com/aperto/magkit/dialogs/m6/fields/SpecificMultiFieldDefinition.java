package com.aperto.magkit.dialogs.m6.fields;

import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.JcrMultiFieldDefinition;

/**
 * Multi field with specific size.
 *
 * @author payam.tabrizi
 * @since 22.02.21
 */
@FieldType("specificMultiField")
public class SpecificMultiFieldDefinition extends JcrMultiFieldDefinition {

    private Long _maxComponents;

    public SpecificMultiFieldDefinition() {
        setImplementationClass((Class) SpecificMultiFormView.class);
    }

    public Long getMaxComponents() {
        return _maxComponents;
    }

    public void setMaxComponents(Long maxComponents) {
        _maxComponents = maxComponents;
    }
}
