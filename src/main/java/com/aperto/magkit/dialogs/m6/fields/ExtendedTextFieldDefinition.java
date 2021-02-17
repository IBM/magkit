package com.aperto.magkit.dialogs.m6.fields;

import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.TextFieldDefinition;

/**
 * Extends the ordinary {@link TextFieldDefinition}.
 *
 * @author Janine.Kleessen
 * @since 17.02.2021
 */
@FieldType("extendedTextField")
public class ExtendedTextFieldDefinition extends TextFieldDefinition {

    public ExtendedTextFieldDefinition() {
        setFactoryClass(ExtendedTextFieldFactory.class);
    }

    private int _recommendedLength = -1;

    public int getRecommendedLength() {
        return _recommendedLength;
    }

    public void setRecommendedLength(int recommendedLength) {
        _recommendedLength = recommendedLength;
    }

}