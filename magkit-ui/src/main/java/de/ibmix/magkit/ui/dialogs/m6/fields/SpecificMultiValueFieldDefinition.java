package de.ibmix.magkit.ui.dialogs.m6.fields;

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
 * Multi field with specific size. Used for simple multi fields stored in a multi value property.
 *
 * @author payam.tabrizi
 * @since 22.02.21
 */
@FieldType("specificMultiValueField")
public class SpecificMultiValueFieldDefinition extends JcrMultiValueFieldDefinition implements SpecificMultiDefinition {

    private Long _maxComponents = DEFAULT_MAX;
    private String _parentCountProperty;

    public SpecificMultiValueFieldDefinition() {
        setImplementationClass((Class) SpecificMultiFormView.class);
    }

    public Long getMaxComponents() {
        return _maxComponents;
    }

    public void setMaxComponents(Long maxComponents) {
        _maxComponents = maxComponents;
    }

    public String getParentComponentProperty() {
        return _parentCountProperty;
    }

    public void setParentCountProperty(final String parentCountProperty) {
        _parentCountProperty = parentCountProperty;
    }
}
