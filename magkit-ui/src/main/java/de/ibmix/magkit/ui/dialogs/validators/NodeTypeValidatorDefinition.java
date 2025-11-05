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

import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.field.ValidatorType;
import java.util.Collection;

/**
 * Definition specifying accepted JCR node types for {@link NodeTypeValidator}.
 * <p>Provides a collection of type names; empty collection permits any type.</p>
 * @author ngoc.tran
 * @since 2024-03-22
 */
@ValidatorType("nodeTypeValidator")
public class NodeTypeValidatorDefinition extends ConfiguredFieldValidatorDefinition {
    private Collection<String> _acceptedNodeTypes;

    public NodeTypeValidatorDefinition() {
        setFactoryClass(NodeTypeValidatorFactory.class);
    }

    public Collection<String> getAcceptedNodeTypes() {
        return _acceptedNodeTypes;
    }

    public void setAcceptedNodeTypes(Collection<String> acceptedNodeTypes) {
        _acceptedNodeTypes = acceptedNodeTypes;
    }
}
