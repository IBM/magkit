package com.aperto.magkit.dialogs.fields;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Field;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.factory.MultiValueFieldFactory;

/**
 * Multi field with specific size.
 *
 * @author diana.racho (Aperto AG)
 */
public class SpecificMultiValueFieldFactory extends MultiValueFieldFactory<SpecificMultiFieldDefinition> {

    private FieldFactoryFactory _fieldFactoryFactory;
    private I18NAuthoringSupport _i18nAuthoringSupport;
    private ComponentProvider _componentProvider;

    public SpecificMultiValueFieldFactory(SpecificMultiFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, FieldFactoryFactory fieldFactoryFactory, I18NAuthoringSupport i18nAuthoringSupport, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport, fieldFactoryFactory, componentProvider);
        _fieldFactoryFactory = fieldFactoryFactory;
        _componentProvider = componentProvider;
        _i18nAuthoringSupport = i18nAuthoringSupport;
    }

    @Override
    protected Field<PropertysetItem> createFieldComponent() {
        return new SpecificMultiField(definition, _fieldFactoryFactory, _componentProvider, item, _i18nAuthoringSupport);
    }
}