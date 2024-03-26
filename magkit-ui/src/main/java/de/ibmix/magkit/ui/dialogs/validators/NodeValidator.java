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

import com.vaadin.data.ValidationResult;
import com.vaadin.data.ValueContext;
import com.vaadin.data.validator.AbstractValidator;
import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;

import javax.jcr.Node;

/**
 * Basic NodeValidator that checks if the node exists.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-03-22
 */
public class NodeValidator extends AbstractValidator<Node> {
    /**
     * Constructs a validator with the given error message. The substring "{0}"
     * is replaced by the value that failed validation.
     *
     * @param validatorDefinition the ConfiguredFieldValidatorDefinition that contains the error message, not null
     */
    protected NodeValidator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        super(validatorDefinition.getErrorMessage());
    }

    @Override
    public ValidationResult apply(Node value, ValueContext context) {
        return toResult(value, isValidValue(value));
    }

    public boolean isValidValue(Node value) {
        return value != null;
    };
}
