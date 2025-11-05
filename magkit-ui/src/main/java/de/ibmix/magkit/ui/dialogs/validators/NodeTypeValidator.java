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
 * Validator ensuring a node's primary or mixin type matches any configured accepted types.
 * <p>Checks primary type via {@link Node#isNodeType(String)} and iterates configured accepted types for a match.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Supports empty accepted list (treats as always valid).</li>
 *   <li>Graceful exception handling during type inspection – logs and treats as non-match.</li>
 * </ul>
 *
 * <p>Thread-safety: Not thread-safe; instance per field binding.</p>
 * @author ngoc.tran
 * @since 2024-03-22
 */
@Slf4j
public class NodeTypeValidator extends AbstractValidator<Node> {

    private final NodeTypeValidatorDefinition _definition;

    public NodeTypeValidator(NodeTypeValidatorDefinition definition) {
        super(definition.getErrorMessage());
        _definition = definition;
    }

    /**
     * Validate node against accepted node type list.
     * @param node node (may be null)
     * @return true if valid
     */
    public boolean isValidValue(Node node) {
        boolean valid = true;
        if (node != null) {
            valid = isEmpty(_definition.getAcceptedNodeTypes()) || isAcceptedNodeType(node);
        }
        return valid;
    }

    /**
     * Safe node type check encapsulating JCR exceptions.
     * @param node node to inspect
     * @param nodeType candidate type name
     * @return true if node reports the type
     */
    private boolean isNodeType(Node node, String nodeType) {
        try {
            return node.isNodeType(nodeType);
        } catch (RepositoryException e) {
            LOGGER.error("Unable to check type for node.", e);
        }
        return false;
    }

    /**
     * Iterate accepted node types for first match.
     * @param node node to validate
     * @return true if any accepted type matches
     */
    private boolean isAcceptedNodeType(Node node) {
        for (String acceptedNodeType : _definition.getAcceptedNodeTypes()) {
            if (isNodeType(node, acceptedNodeType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vaadin apply hook mapping validity to {@link ValidationResult}.
     * @param value node value
     * @param context value context
     * @return validation outcome
     */
    @Override
    public ValidationResult apply(Node value, ValueContext context) {
        return toResult(value, isValidValue(value));
    }
}
