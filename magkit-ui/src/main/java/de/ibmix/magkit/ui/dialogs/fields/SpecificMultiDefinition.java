package de.ibmix.magkit.ui.dialogs.fields;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
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

/**
 * Common contract for multi field definitions with size constraints.
 * <p>
 * Provides accessors used by {@link SpecificMultiFormView} to determine allowed component counts and optional linkage
 * to a parent component property for advanced counting logic.
 * </p>
 * <p>Implementations: {@link SpecificMultiFieldDefinition}, {@link SpecificMultiValueFieldDefinition}.</p>
 *
 * @author frank.sommer
 * @since 2021-03-05
 */
public interface SpecificMultiDefinition {
    long DEFAULT_MAX = 3;

    /**
     * @return maximum allowed components (null indicates default)
     */
    Long getMaxComponents();

    /**
     * @return optional parent component property name for contextual counting
     */
    String getParentComponentProperty();
}
