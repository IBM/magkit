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

import java.util.Collection;
import java.util.Collections;

/**
 * Base definition supplying a collection of accepted string values for derived validators.
 * <p>Used by template id/type validators to centralize list logic.</p>
 * @author wolf.bubenik@ibmix.de
 * @since 2024-03-22
 */
public class StringValuesValidatorDefinition extends ConfiguredFieldValidatorDefinition {
    private Collection<String> _acceptedValues;

    public Collection<String> getAcceptedValues() {
        return _acceptedValues == null ? Collections.emptyList() : _acceptedValues;
    }

    public void setAcceptedValues(Collection<String> acceptedNodeTypes) {
        _acceptedValues = acceptedNodeTypes;
    }

    /**
     * Check if value is among accepted values or list empty (wildcard acceptance).
     * @param value value to test
     * @return true if accepted
     */
    public boolean hasAcceptedValue(String value) {
        return getAcceptedValues().isEmpty() || _acceptedValues.contains(value);
    }
}
