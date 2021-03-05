package com.aperto.magkit.dialogs.m6.fields;

import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.JcrMultiFieldDefinition;

/**
 * Multi field with specific size. Used for multi fields containing composite fields.
 *
 * @author payam.tabrizi
 * @since 22.02.21
 */
@FieldType("specificMultiField")
public class SpecificMultiFieldDefinition extends JcrMultiFieldDefinition implements SpecificMultiDefinition {
    private Long _maxCount = DEFAULT_MAX;
    private String _parentCountProperty;

    public SpecificMultiFieldDefinition() {
        setImplementationClass((Class) SpecificMultiFormView.class);
    }

    public Long getMaxComponent() {
        return _maxCount;
    }

    public void setMaxCount(Long maxCount) {
        _maxCount = maxCount;
    }

    public String getParentComponentProperty() {
        return _parentCountProperty;
    }

    public void setParentCountProperty(final String parentCountProperty) {
        _parentCountProperty = parentCountProperty;
    }
}
