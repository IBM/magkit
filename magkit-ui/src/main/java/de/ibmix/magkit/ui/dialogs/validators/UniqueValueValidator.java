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
import de.ibmix.magkit.core.utils.NodeUtils;
import de.ibmix.magkit.query.NodesByQuery;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Item;
import javax.jcr.Node;
import java.util.List;

/**
 * Validator ensuring a string value is unique among nodes matching workspace/nodeType/property constraints.
 * <p>Executes a query via {@link NodesByQuery} to find nodes with the same property value; allows match with itself.</p>
 * <p>Key features:
 * <ul>
 *   <li>Workspace-scoped uniqueness check.</li>
 *   <li>Property-based filtering across a node type.</li>
 *   <li>Self-update allowance (ignores the current node's own value).</li>
 * </ul>
 * </p>
 * <p>Null & config handling: If mandatory config values missing, validation returns false (not valid) or treats as valid? Here it's false until properly configured.</p>
 * @author frank.sommer
 * @since 2024-03-12
 */
@Slf4j
public class UniqueValueValidator extends AbstractValidator<String> {

    private final UniqueValueValidatorDefinition _definition;
    private final info.magnolia.ui.ValueContext<Item> _itemContext;

    public UniqueValueValidator(UniqueValueValidatorDefinition definition, info.magnolia.ui.ValueContext<Item> itemContext) {
        super(definition.getErrorMessage());
        _definition = definition;
        _itemContext = itemContext;
    }

    /**
     * Vaadin apply hook mapping uniqueness to a ValidationResult.
     * @param value candidate value
     * @param context value context (unused)
     * @return validation result
     */
    @Override
    public ValidationResult apply(String value, ValueContext context) {
        return toResult(value, isValidValue(value));
    }

    /**
     * Determine uniqueness of value among matching nodes (excluding current node).
     * @param value candidate value (may be null)
     * @return true if unique or only present on current node
     */
    private boolean isValidValue(String value) {
        boolean valid = false;
        final String workspace = _definition.getWorkspace();
        final String nodeType = _definition.getNodeType();
        final String propertyName = _definition.getPropertyName();
        if (workspace != null && nodeType != null && propertyName != null) {
            LOGGER.debug("Validate for unique value {} by query [{},{},{}].", value, workspace, nodeType, propertyName);
            final List<Node> foundNodes = new NodesByQuery(workspace, nodeType, propertyName).apply(value);
            final String currentNodeId = NodeUtils.getIdentifier((Node) _itemContext.getSingle().orElse(null));
            valid = foundNodes.isEmpty() || foundNodes.size() == 1 && currentNodeId.equals(NodeUtils.getIdentifier(foundNodes.get(0)));
        }
        return valid;
    }
}
