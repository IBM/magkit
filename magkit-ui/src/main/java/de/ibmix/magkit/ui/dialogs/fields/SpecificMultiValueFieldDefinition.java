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

import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.JcrMultiValueFieldDefinition;

/**
 * Definition for a simple multi-value field stored in a single multi-value property with a configurable size limit.
 * <p>
 * Suitable for lists of primitive values (strings, numbers) rather than composite groups. Adds maximum component
 * configuration similar to {@link SpecificMultiFieldDefinition} but leverages {@link JcrMultiValueFieldDefinition}.
 * </p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Configurable maximum number of values (defaults to {@link #DEFAULT_MAX}).</li>
 *   <li>Optional parent count property name for contextual constraints.</li>
 *   <li>Uses same view implementation {@link SpecificMultiFormView} for consistent UI behavior.</li>
 * </ul>
 *
 * <p>Thread-safety: Not thread-safe; configure once during dialog initialization.</p>
 *
 * @author payam.tabrizi
 * @since 2021-02-22
 */
@FieldType("specificMultiValueField")
public class SpecificMultiValueFieldDefinition extends JcrMultiValueFieldDefinition implements SpecificMultiDefinition {

    private Long _maxComponents = DEFAULT_MAX;
    private String _parentCountProperty;

    public SpecificMultiValueFieldDefinition() {
        setImplementationClass((Class) SpecificMultiFormView.class);
    }

    /**
     * @return maximum allowed values or default
     */
    public Long getMaxComponents() {
        return _maxComponents;
    }

    /**
     * Set maximum allowed values.
     * @param maxComponents new maximum (null falls back to default)
     */
    public void setMaxComponents(Long maxComponents) {
        _maxComponents = maxComponents;
    }

    /**
     * Name of related parent count property.
     * @return property name or null
     */
    public String getParentComponentProperty() {
        return _parentCountProperty;
    }

    /**
     * Configure parent count property name.
     * @param parentCountProperty property name reference
     */
    public void setParentCountProperty(final String parentCountProperty) {
        _parentCountProperty = parentCountProperty;
    }
}
