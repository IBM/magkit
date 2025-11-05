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
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.field.AbstractFieldValidatorFactory;

import javax.jcr.Item;

/**
 * Factory producing {@link UniqueValueValidator} using the provided {@link UniqueValueValidatorDefinition} and item context.
 * <p>Encapsulates wiring of definition plus current item context to validator logic.</p>
 * @author frank.sommer
 * @since 2024-03-12
 */
public class UniqueValueValidatorFactory extends AbstractFieldValidatorFactory<UniqueValueValidatorDefinition, String> {
    private final ValueContext<Item> _valueContext;

    public UniqueValueValidatorFactory(UniqueValueValidatorDefinition definition, ValueContext<Item> valueContext) {
        super(definition);
        _valueContext = valueContext;
    }

    @Override
    public Validator<String> createValidator() {
        return new UniqueValueValidator(definition, _valueContext);
    }
}
