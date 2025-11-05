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
 * Base validator ensuring a node reference is non-null.
 * <p>Intended as superclass for specialized node validators adding further constraints (type, template).</p>
 * <p>Key features: Simple existence check; integrates with Vaadin validation API.</p>
 * <p>Null handling: <code>null</code> is invalid.</p>
 * <p>Thread-safety: Stateless apart from error message.</p>
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

    /**
     * Vaadin apply hook producing a result based on {@link #isValidValue(Node)}.
     * @param value node value
     * @param context value context
     * @return validation result
     */
    @Override
    public ValidationResult apply(Node value, ValueContext context) {
        return toResult(value, isValidValue(value));
    }

    /**
     * Basic non-null check; subclasses extend for additional logic.
     * @param value node value (may be null)
     * @return true if non-null
     */
    public boolean isValidValue(Node value) {
        return value != null;
    };
}
