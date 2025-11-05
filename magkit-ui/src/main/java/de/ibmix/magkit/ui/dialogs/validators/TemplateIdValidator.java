package de.ibmix.magkit.ui.dialogs.validators;

/*-
 * #%L
 * IBM iX Magnolia Kit UI
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
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

import de.ibmix.magkit.core.utils.NodeUtils;

import javax.jcr.Node;

/**
 * Validator checking that a node's template ID matches one of the accepted values from its definition.
 * <p>Relies on {@link NodeUtils#getTemplate(Node)} to resolve the template metadata.</p>
 * <p>Thread-safety: Not thread-safe; one instance per field usage.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-03-22
 */
public class TemplateIdValidator extends NodeValidator {

    private TemplateIdValidatorDefinition _definition;

    /**
     * Constructs a validator with the given ValidatorDefinition.
     *
     * @param validatorDefinition the ConfiguredFieldValidatorDefinition that contains the error message, not null
     */
    protected TemplateIdValidator(TemplateIdValidatorDefinition validatorDefinition) {
        super(validatorDefinition);
        _definition = validatorDefinition;
    }

    /**
     * Evaluate node validity including template ID constraint.
     * @param node JCR node (may be null)
     * @return true if node exists and template id is accepted
     */
    public boolean isValidValue(Node node) {
        return super.isValidValue(node) && _definition.hasAcceptedValue(NodeUtils.getTemplate(node));
    }
}
