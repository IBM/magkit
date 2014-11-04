package com.aperto.magkit.dialogs.fields;

import info.magnolia.i18nsystem.I18nText;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;

import java.util.ArrayList;
import java.util.List;

/**
 * List select field definition as alternative for an option group.
 *
 * @author frank.sommer
 * @since 21.10.14
 */
public class ListSelectFieldDefinition extends OptionGroupFieldDefinition {
    private static final int DEF_ROWS = 6;
    protected static final String EMPTY_VALUE = "---";

    private int _rows = DEF_ROWS;
    private String _emptySelectionLabel = "listSelect.emptySelection.label";

    @SuppressWarnings({"unchecked", "RedundantCast"})
    public ListSelectFieldDefinition() {
        setTransformerClass((Class<? extends Transformer<?>>) (Object) ListSelectTransformer.class);
    }

    public int getRows() {
        return _rows;
    }

    public void setRows(final int rows) {
        _rows = rows;
    }

    @I18nText
    public String getEmptySelectionLabel() {
        return _emptySelectionLabel;
    }

    public void setEmptySelectionLabel(final String emptySelectionLabel) {
        _emptySelectionLabel = emptySelectionLabel;
    }

    @Override
    public List<SelectFieldOptionDefinition> getOptions() {
        List<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();
        if (!isRequired()) {
            SelectFieldOptionDefinition emptyOption = new SelectFieldOptionDefinition();
            emptyOption.setLabel(getEmptySelectionLabel());
            emptyOption.setValue(EMPTY_VALUE);
            options.add(emptyOption);
        }
        options.addAll(super.getOptions());
        return options;
    }
}
