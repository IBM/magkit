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

import info.magnolia.ui.form.config.MultiValueFieldBuilder;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;

/**
 * Builder for the {@link SpecificMultiField} instance.
 *
 * @author noreply@aperto.com
 * @deprecated use new ui 6 field
 */
@Deprecated(since = "3.5.2")
public class SpecificMultiValueFieldBuilder extends MultiValueFieldBuilder {

    private SpecificMultiFieldDefinition _definition;

    public SpecificMultiValueFieldBuilder(String name) {
        super(name);
    }

    @Override
    // CHECKSTYLE:OFF
    public MultiValueFieldDefinition definition() {
        if (_definition == null) {
            _definition = new SpecificMultiFieldDefinition();
        }
        return _definition;
    }

    private SpecificMultiFieldDefinition typedDefinition() {
        return (SpecificMultiFieldDefinition) definition();
    }
    // CHECKSTYLE:ON

    public SpecificMultiValueFieldBuilder setParentCountFieldName(String parentCountFieldName) {
        typedDefinition().setParentCountFieldName(parentCountFieldName);
        return this;
    }

    public SpecificMultiValueFieldBuilder setCount(Long count) {
        typedDefinition().setCount(count);
        return this;
    }

}
