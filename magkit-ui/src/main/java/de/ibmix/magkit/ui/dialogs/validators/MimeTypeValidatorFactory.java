package de.ibmix.magkit.ui.dialogs.validators;

/*-
 * #%L
 * IBM iX Magnolia Kit UI
 * %%
 * Copyright (C) 2023 - 2024 IBM iX
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

import com.vaadin.data.Validator;
import info.magnolia.dam.api.Item;
import info.magnolia.ui.field.AbstractFieldValidatorFactory;

/**
 * Mime type validator factory.
 *
 * @author frank.sommer
 */
public class MimeTypeValidatorFactory extends AbstractFieldValidatorFactory<MimeTypeValidatorDefinition, Item> {
    private final MimeTypeValidatorDefinition _definition;

    public MimeTypeValidatorFactory(MimeTypeValidatorDefinition definition) {
        super(definition);
        _definition = definition;
    }

    @Override
    public Validator<Item> createValidator() {
        return new MimeTypeValidator(_definition);
    }
}
