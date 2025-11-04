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
import info.magnolia.ui.field.TextFieldDefinition;

/**
 * Extension of {@link TextFieldDefinition} providing an additional recommended length hint for editorial guidance.
 * <p>
 * When the field has no enforced maximum length but a recommended length is set (&gt; 0), the factory will wrap the
 * underlying Vaadin text field with {@link ExtendedTextField} adding a remaining length indicator label.
 * </p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Recommended length separate from max length.</li>
 *   <li>Factory-controlled decoration only when max length is not set.</li>
 * </ul>
 *
 * <p>Thread-safety: Not thread-safe; definition instances are configuration objects.</p>
 *
 * @author Janine.Kleessen
 * @since 2021-02-17
 */
@FieldType("extendedTextField")
public class ExtendedTextFieldDefinition extends TextFieldDefinition {

    private int _recommendedLength = -1;

    public ExtendedTextFieldDefinition() {
        setFactoryClass(ExtendedTextFieldFactory.class);
    }

    /**
     * Recommended length for editorial display when max length &lt; 1.
     * @return recommended length or -1 if not set
     */
    public int getRecommendedLength() {
        return _recommendedLength;
    }

    /**
     * Set recommended length used for remaining-length indicator.
     * @param recommendedLength positive recommended length or -1 to disable
     */
    public void setRecommendedLength(int recommendedLength) {
        _recommendedLength = recommendedLength;
    }

}
