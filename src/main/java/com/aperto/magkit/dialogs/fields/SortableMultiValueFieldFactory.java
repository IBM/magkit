package com.aperto.magkit.dialogs.fields;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Field;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.transformer.Transformer;

import javax.inject.Inject;

/**
 * Copy of Magnolia Class with a small change.
 *
 * @author Stefan Jahn
 * @since 02.12.14
 */
public class SortableMultiValueFieldFactory extends AbstractFieldFactory<SortableMultiValueFieldDefinition, PropertysetItem> {

    private final Item _relatedFieldItem;
    private final FieldFactoryFactory _fieldFactoryFactory;
    private final ComponentProvider _componentProvider;
    private final I18nContentSupport _i18nContentSupport;

    @Inject
    public SortableMultiValueFieldFactory(final SortableMultiValueFieldDefinition definition, final Item relatedFieldItem, final FieldFactoryFactory fieldFactoryFactory, final ComponentProvider componentProvider, final I18nContentSupport
            i18nContentSupport) {
        super(definition, relatedFieldItem);
        _relatedFieldItem = relatedFieldItem;
        _fieldFactoryFactory = fieldFactoryFactory;
        _componentProvider = componentProvider;
        _i18nContentSupport = i18nContentSupport;
    }


    @Override
    protected Field<PropertysetItem> createFieldComponent() {
        // FIXME change i18n setting : MGNLUI-1548
        SortableMultiValueFieldDefinition fieldDefinition = getFieldDefinition();
        fieldDefinition.setI18nBasename(getMessages().getBasename());

        SortableMultiValueField field = new SortableMultiValueField(fieldDefinition, _fieldFactoryFactory, _componentProvider, item, _i18nContentSupport);
        // Set Caption
        field.setButtonCaptionAdd(getMessage(fieldDefinition.getButtonSelectAddLabel()));
        field.setButtonCaptionRemove(getMessage(fieldDefinition.getButtonSelectRemoveLabel()));

        return field;
    }

    /**
     * Create a new Instance of {@link info.magnolia.ui.form.field.transformer.Transformer}.
     */
    @Override
    protected Transformer<?> initializeTransformer(Class<? extends Transformer<?>> transformerClass) {
        return _componentProvider.newInstance(transformerClass, getItem(), getFieldDefinition(), PropertysetItem.class);
    }

    public Item getItem() {
        return _relatedFieldItem;
    }
}
