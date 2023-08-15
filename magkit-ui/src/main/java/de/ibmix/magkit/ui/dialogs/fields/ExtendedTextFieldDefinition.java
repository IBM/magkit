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

import info.magnolia.ui.form.field.definition.TextFieldDefinition;

/**
 * Extends the ordinary {@link TextFieldDefinition}.
 *
 * @author Stefan Jahn
 * @since 21.11.14
 * @deprecated use new ui 6 field {@link de.ibmix.magkit.ui.dialogs.m6.fields.ExtendedTextFieldDefinition}
 */
@Deprecated(since = "3.5.2")
public class ExtendedTextFieldDefinition extends TextFieldDefinition {

    private int _recommendedLength = -1;

    public int getRecommendedLength() {
        return _recommendedLength;
    }

    public void setRecommendedLength(int recommendedLength) {
        _recommendedLength = recommendedLength;
    }
}
