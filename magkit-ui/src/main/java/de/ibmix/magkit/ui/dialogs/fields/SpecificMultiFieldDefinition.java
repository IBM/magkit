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
import info.magnolia.ui.field.JcrMultiFieldDefinition;

/**
 * Definition for a multi-value field containing composite child field sets with a configurable maximum number of components.
 * <p>
 * The field is intended for structured repeatable groups (e.g. list of teaser composites) where each entry itself can
 * contain multiple subfields. This definition adds sizing constraints beyond the base {@link JcrMultiFieldDefinition}.
 * </p>
 * <p>Key features:
 * <ul>
 *   <li>Configurable maximum component count (defaults to {@link #DEFAULT_MAX}).</li>
 *   <li>Optional parent count property to relate this multi field to a parent counter.</li>
 *   <li>Uses {@link SpecificMultiFormView} as implementation to enforce add-button enable/disable logic.</li>
 * </ul>
 * </p>
 * <p>Usage preconditions: Configure the field in a Magnolia dialog definition with type <code>specificMultiField</code>.
 * Provide a composite field definition as the field's field definition so all children are grouped per entry.</p>
 * <p>Thread-safety: Configuration objects are not thread-safe; use only in Magnolia UI initialization phase.</p>
 *
 * @author payam.tabrizi
 * @since 2021-02-22
 */
@FieldType("specificMultiField")
public class SpecificMultiFieldDefinition extends JcrMultiFieldDefinition implements SpecificMultiDefinition {
    private Long _maxComponents = DEFAULT_MAX;
    private String _parentCountProperty;

    public SpecificMultiFieldDefinition() {
        setImplementationClass((Class) SpecificMultiFormView.class);
    }

    /**
     * Maximum allowed component entries in the multi field.
     * @return configured maximum or default
     */
    public Long getMaxComponents() {
        return _maxComponents;
    }

    /**
     * Set maximum allowed component entries.
     * @param maxComponents new maximum (may be null to fall back to default)
     */
    public void setMaxComponents(Long maxComponents) {
        _maxComponents = maxComponents;
    }

    /**
     * Name of a parent component property used for relational counting.
     * @return parent count property name or null
     */
    public String getParentComponentProperty() {
        return _parentCountProperty;
    }

    /**
     * Configure the parent count property name.
     * @param parentCountProperty property name to reference for counting logic
     */
    public void setParentCountProperty(final String parentCountProperty) {
        _parentCountProperty = parentCountProperty;
    }
}
