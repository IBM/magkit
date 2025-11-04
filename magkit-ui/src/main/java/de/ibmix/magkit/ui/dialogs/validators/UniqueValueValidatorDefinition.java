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
import lombok.Getter;
import lombok.Setter;

/**
 * Definition for a validator ensuring uniqueness of a string property across nodes of a given type/workspace.
 * <p>Key features:</p>
 * <ul>
 *   <li>Targets a specific property (e.g. "title") within nodes of a configured JCR node type.</li>
 *   <li>Restricts search scope to a specified workspace for performance and clarity.</li>
 *   <li>Works together with {@link UniqueValueValidatorFactory} / {@link UniqueValueValidator} to execute a query.</li>
 * </ul>
 *
 * <p>Usage preconditions: Configure <code>propertyName</code>, <code>nodeType</code> and <code>workspace</code>; missing values disable validation logic.</p>
 * <p>Null and error handling: Missing configuration values are treated gracefully by the validator (validation passes).</p>
 * <p>Thread-safety: Definition instances are configuration objects; not thread-safe, use only at initialization.</p>
 *
 * @author frank.sommer
 * @since 2024-03-12
 */
@Setter
@Getter
@ValidatorType("uniqueValueValidator")
public class UniqueValueValidatorDefinition extends ConfiguredFieldValidatorDefinition {
    private String _propertyName;
    private String _nodeType;
    private String _workspace;

    public UniqueValueValidatorDefinition() {
        setFactoryClass(UniqueValueValidatorFactory.class);
    }
}
