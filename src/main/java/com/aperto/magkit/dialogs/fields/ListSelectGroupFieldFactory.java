package com.aperto.magkit.dialogs.fields;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.ListSelect;
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
 * @deprecated use new ui 6 field {@link info.magnolia.ui.field.factory.ListSelectFieldFactory}
 */
@Deprecated(since = "3.5.2")
public class ListSelectGroupFieldFactory extends OptionGroupFieldFactory<ListSelectFieldDefinition> {

    @Inject
    public ListSelectGroupFieldFactory(final ListSelectFieldDefinition definition, final Item relatedFieldItem, final UiContext uiContext, final I18NAuthoringSupport i18nAuthoringSupport, final ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport, componentProvider);
    }


    @Override
    protected AbstractSelect createSelectionField() {
        ListSelect listSelect = new ListSelect();
        listSelect.setRows(((ListSelectFieldDefinition) definition).getRows());
        return listSelect;
    }
}
