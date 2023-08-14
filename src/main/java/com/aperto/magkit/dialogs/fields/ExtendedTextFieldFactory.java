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
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.v7.ui.Field;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.factory.TextFieldFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Factory for {@link ExtendedTextField}.
 *
 * @author Stefan Jahn
 * @since 21.11.14
 * @deprecated use new ui 6 field {@link com.aperto.magkit.dialogs.m6.fields.ExtendedTextFieldFactory}
 */
@Deprecated(since = "3.5.2")
public class ExtendedTextFieldFactory extends TextFieldFactory {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExtendedTextFieldFactory.class);

    @Inject
    public ExtendedTextFieldFactory(final ExtendedTextFieldDefinition definition, final Item relatedFieldItem, final UiContext uiContext, final I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
    }

    /**
     * Wraps the defined text field as extended text field.
     */
    @Override
    protected Field<String> createFieldComponent() {
        Field<String> fieldComponent = super.createFieldComponent();

        if (definition instanceof ExtendedTextFieldDefinition) {
            if (definition.getMaxLength() < 1 && ((ExtendedTextFieldDefinition) definition).getRecommendedLength() > 0) {
                fieldComponent = new ExtendedTextField((ExtendedTextFieldDefinition) definition, (AbstractTextField) fieldComponent);
            }
        }

        return fieldComponent;
    }
}
