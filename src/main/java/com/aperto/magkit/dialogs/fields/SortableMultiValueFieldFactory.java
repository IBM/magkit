package com.aperto.magkit.dialogs.fields;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.PropertysetItem;
import com.vaadin.v7.ui.Field;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.context.UiContext;
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
 * @deprecated use new ui 6 field
 */
@Deprecated(since = "3.5.2")
public class SortableMultiValueFieldFactory extends AbstractFieldFactory<SortableMultiValueFieldDefinition, PropertysetItem> {

    private final Item _relatedFieldItem;
    private final FieldFactoryFactory _fieldFactoryFactory;
    private final ComponentProvider _componentProvider;
    private final I18NAuthoringSupport _i18nAuthoringSupport;

    @Inject
    public SortableMultiValueFieldFactory(final SortableMultiValueFieldDefinition definition, final Item relatedFieldItem, UiContext uiContext, final FieldFactoryFactory fieldFactoryFactory, final ComponentProvider componentProvider, final I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
        _relatedFieldItem = relatedFieldItem;
        _fieldFactoryFactory = fieldFactoryFactory;
        _componentProvider = componentProvider;
        _i18nAuthoringSupport = i18nAuthoringSupport;
    }


    @Override
    protected Field<PropertysetItem> createFieldComponent() {
        // FIXME change i18n setting : MGNLUI-1548
        SortableMultiValueFieldDefinition fieldDefinition = getFieldDefinition();
        fieldDefinition.setI18nBasename(getMessages().getBasename());

        SortableMultiValueField field = new SortableMultiValueField(fieldDefinition, _fieldFactoryFactory, _componentProvider, item, _i18nAuthoringSupport);
        // Set Caption
        field.setButtonCaptionAdd(getMessage(fieldDefinition.getButtonSelectAddLabel()));
        field.setButtonCaptionRemove(getMessage(fieldDefinition.getButtonSelectRemoveLabel()));

        return field;
    }

    /**
     * Create a new Instance of {@link info.magnolia.ui.form.field.transformer.Transformer}.
     */
    @Override
    protected Transformer<?> initializeTransformer(final Class<? extends Transformer<?>> transformerClass) {
        return _componentProvider.newInstance(transformerClass, getItem(), getFieldDefinition(), PropertysetItem.class);
    }

    public Item getItem() {
        return _relatedFieldItem;
    }
}
