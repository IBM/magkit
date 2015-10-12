package com.aperto.magkit.dialogs.fields;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Field;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.factory.MultiValueFieldFactory;

/**
 * Multi field with specific size.
 *
 * @author diana.racho (Aperto AG)
 */
public class SpecificMultiValueFieldFactory extends MultiValueFieldFactory<SpecificMultiFieldDefinition> {

    private FieldFactoryFactory _fieldFactoryFactory;
    private I18nContentSupport _i18nContentSupport;
    private ComponentProvider _componentProvider;

    public SpecificMultiValueFieldFactory(MultiValueFieldDefinition definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, fieldFactoryFactory, i18nContentSupport, componentProvider);
        _fieldFactoryFactory = fieldFactoryFactory;
        _componentProvider = componentProvider;
        _i18nContentSupport = i18nContentSupport;
    }

    @Override
    protected Field<PropertysetItem> createFieldComponent() {
        return new SpecificMultiField(definition, _fieldFactoryFactory, _i18nContentSupport, _componentProvider, item);
    }
}