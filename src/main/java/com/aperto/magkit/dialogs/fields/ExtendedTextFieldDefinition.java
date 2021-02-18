package com.aperto.magkit.dialogs.fields;

import info.magnolia.ui.form.field.definition.TextFieldDefinition;

/**
 * Extends the ordinary {@link TextFieldDefinition}.
 *
 * @author Stefan Jahn
 * @since 21.11.14
 * @deprecated use new ui 6 field {@link com.aperto.magkit.dialogs.m6.fields.ExtendedTextFieldDefinition}
 */
@Deprecated(since = "3.5.2")
public class ExtendedTextFieldDefinition extends TextFieldDefinition {

    private int _recommendedLength = -1;

    public int getRecommendedLength() {
        return _recommendedLength;
    }

    public void setRecommendedLength(int recommendedLength) {
        _recommendedLength = recommendedLength;
    }
}