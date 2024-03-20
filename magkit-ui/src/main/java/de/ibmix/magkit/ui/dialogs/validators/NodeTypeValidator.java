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
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Validator für Knotentypen.
 *
 * @author ngoc.tran
 */
@Slf4j
public class NodeTypeValidator extends AbstractValidator<Node> {

    private final NodeTypeValidatorDefinition _definition;

    public NodeTypeValidator(NodeTypeValidatorDefinition definition) {
        super(definition.getErrorMessage());
        _definition = definition;
    }

    public boolean isValidValue(Node node) {
        boolean valid = true;
        if (node != null) {
            valid = isEmpty(_definition.getAcceptedNodeTypes()) || isAcceptedNodeType(node);
        }
        return valid;
    }

    private boolean isNodeType(Node node, String nodeType) {
        try {
            return node.isNodeType(nodeType);
        } catch (RepositoryException e) {
            LOGGER.error("Unable to check type for node.", e);
        }
        return false;
    }

    private boolean isAcceptedNodeType(Node node) {
        for (String acceptedNodeType : _definition.getAcceptedNodeTypes()) {
            if (isNodeType(node, acceptedNodeType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ValidationResult apply(Node value, ValueContext context) {
        return toResult(value, isValidValue(value));
    }
}
