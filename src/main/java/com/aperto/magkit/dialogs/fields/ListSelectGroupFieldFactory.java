package com.aperto.magkit.dialogs.fields;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ListSelect;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.factory.OptionGroupFieldFactory;

/**
 * Factory for displaying options in a select list.
 *
 * @author frank.sommer
 * @since 21.10.14
 */
public class ListSelectGroupFieldFactory extends OptionGroupFieldFactory<ListSelectFieldDefinition> {

    public ListSelectGroupFieldFactory(final ListSelectFieldDefinition definition, final Item relatedFieldItem, final ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, componentProvider);
    }

    @Override
    protected AbstractSelect createSelectionField() {
        ListSelect listSelect = new ListSelect();
        listSelect.setRows(((ListSelectFieldDefinition) definition).getRows());
        return listSelect;
    }
}
