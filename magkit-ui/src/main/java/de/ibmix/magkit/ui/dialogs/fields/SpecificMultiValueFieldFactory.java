package de.ibmix.magkit.ui.dialogs.fields;

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
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.factory.MultiValueFieldFactory;

/**
 * Multi field with specific size.
 *
 * @author diana.racho (Aperto AG)
 * @deprecated use new ui 6 field
 */
@Deprecated(since = "3.5.2")
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