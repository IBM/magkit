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

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.field.factory.TextFieldFactory;

import javax.inject.Inject;

/**
 * Factory for {@link ExtendedTextField}.
 *
 * @author Janine.Kleessen
 * @since 17.02.2021
 */
public class ExtendedTextFieldFactory extends TextFieldFactory {

    private final ExtendedTextFieldDefinition _definition;

    @Inject
    public ExtendedTextFieldFactory(final ExtendedTextFieldDefinition definition, ComponentProvider componentProvider) {
        super(definition, componentProvider);
        _definition = definition;
    }

    /**
     * Wraps the defined text field as extended text field.
     */
    @Override
    public Component createFieldComponent() {
        Component fieldComponent = super.createFieldComponent();

        if (_definition.getMaxLength() < 1 && _definition.getRecommendedLength() > 0) {
            fieldComponent = new ExtendedTextField(_definition, (AbstractTextField) fieldComponent);
        }

        return fieldComponent;
    }
}
