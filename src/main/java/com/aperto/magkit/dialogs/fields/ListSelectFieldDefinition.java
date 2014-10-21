package com.aperto.magkit.dialogs.fields;

import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;

/**
 * List select field definition as alternative for an option group.
 *
 * @author frank.sommer
 * @since 21.10.14
 */
public class ListSelectFieldDefinition extends OptionGroupFieldDefinition {
    private static final int DEF_ROWS = 6;

    private int _rows = DEF_ROWS;

    public int getRows() {
        return _rows;
    }

    public void setRows(final int rows) {
        _rows = rows;
    }
}
