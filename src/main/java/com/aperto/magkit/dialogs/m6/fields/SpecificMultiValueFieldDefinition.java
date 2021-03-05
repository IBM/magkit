package com.aperto.magkit.dialogs.m6.fields;

import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.JcrMultiValueFieldDefinition;

/**
 * Multi field with specific size. Used for simple multi fields stored in a multi value property.
 *
 * @author payam.tabrizi
 * @since 22.02.21
 */
@FieldType("specificMultiValueField")
public class SpecificMultiValueFieldDefinition extends JcrMultiValueFieldDefinition implements SpecificMultiDefinition {

    private Long _maxCount = DEFAULT_MAX;
    private String _parentCountProperty;

    public SpecificMultiValueFieldDefinition() {
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
