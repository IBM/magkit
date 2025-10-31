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

import de.ibmix.magkit.core.utils.NodeUtils;

import javax.jcr.Node;

/**
 * Validator enforcing accepted template type values for a node.
 * <p>Retrieves template type via {@link NodeUtils#getTemplateType(Node)} and checks against accepted values list.</p>
 * <p>Null handling: A null node is invalid. A null template type is accepted only when accepted values list is empty (wildcard).</p>
 * @author wolf.bubenik@ibmix.de
 * @since 2024-03-22
 */
public class TemplateTypeValidator extends NodeValidator {

    private final TemplateTypeValidatorDefinition _definition;

    /**
     * Constructs a validator with the given ValidatorDefinition.
     *
     * @param validatorDefinition the ConfiguredFieldValidatorDefinition that contains the error message, not null
     */
    protected TemplateTypeValidator(TemplateTypeValidatorDefinition validatorDefinition) {
        super(validatorDefinition);
        _definition = validatorDefinition;
    }

    /**
     * Validate node template type against accepted values.
     * @param node JCR node (may be null)
     * @return true if node exists and template type accepted
     */
    public boolean isValidValue(Node node) {
        return super.isValidValue(node) && _definition.hasAcceptedValue(NodeUtils.getTemplateType(node));
    }
}
