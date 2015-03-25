package com.aperto.magkit.dialogs.fields;

import info.magnolia.ui.form.field.definition.TextFieldDefinition;

/**
 * Extends the ordinary {@link TextFieldDefinition}.
 *
 * @author Stefan Jahn
 * @since 21.11.14
 */
public class ExtendedTextFieldDefinition extends TextFieldDefinition {

    private int _recommendedLength = -1;

    public int getRecommendedLength() {
        return _recommendedLength;
    }

    public void setRecommendedLength(int recommendedLength) {
        _recommendedLength = recommendedLength;
    }

}