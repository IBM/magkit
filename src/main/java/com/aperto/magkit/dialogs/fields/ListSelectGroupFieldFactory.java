package com.aperto.magkit.dialogs.fields;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ListSelect;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.factory.OptionGroupFieldFactory;

import javax.inject.Inject;

/**
 * Factory for displaying options in a select list.
 *
 * @author frank.sommer
 * @since 21.10.14
 */
public class ListSelectGroupFieldFactory extends OptionGroupFieldFactory<ListSelectFieldDefinition> {

    @Inject
    public ListSelectGroupFieldFactory(ListSelectFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport, componentProvider);
    }


    @Override
    protected AbstractSelect createSelectionField() {
        ListSelect listSelect = new ListSelect();
        listSelect.setRows(((ListSelectFieldDefinition) definition).getRows());
        return listSelect;
    }
}
