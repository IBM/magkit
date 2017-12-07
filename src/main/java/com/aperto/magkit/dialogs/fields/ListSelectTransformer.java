package com.aperto.magkit.dialogs.fields;

import com.vaadin.v7.data.Item;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.ListToSetTransformer;

import java.util.Collection;

/**
 * Transformer filtered empty selection items.
 *
 * @param <T> type of value
 * @author frank.sommer
 * @since 04.11.14
 */
public class ListSelectTransformer<T> extends ListToSetTransformer<T> {

    public ListSelectTransformer(final Item relatedFormItem, final ConfiguredFieldDefinition definition, final Class<T> type, I18NAuthoringSupport i18nAuthoringSupport) {
        super(relatedFormItem, definition, type, i18nAuthoringSupport);
    }

    @Override
    public void writeToItem(T newValue) {
        // remove the empty selection values
        if (newValue instanceof Collection) {
            ((Collection) newValue).remove(ListSelectFieldDefinition.EMPTY_VALUE);
        }
        super.writeToItem(newValue);
    }
}
