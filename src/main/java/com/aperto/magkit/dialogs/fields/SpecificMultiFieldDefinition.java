package com.aperto.magkit.dialogs.fields;

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

import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.transformer.multi.MultiValueTransformer;

/**
 * Multi field with specific size.
 *
 * @author diana.racho (Aperto AG)
 * @deprecated use new ui 6 field {@link com.aperto.magkit.dialogs.m6.fields.SpecificMultiFieldDefinition}
 */
@Deprecated(since = "3.5.2")
public class SpecificMultiFieldDefinition extends MultiValueFieldDefinition {

    private String _parentCountFieldName;
    private Long _count;

    /**
     * Set default {@link info.magnolia.ui.form.field.transformer.Transformer}.
     */
    public SpecificMultiFieldDefinition() {
        setTransformerClass(MultiValueTransformer.class);
    }

    public String getParentCountFieldName() {
        return _parentCountFieldName;
    }

    public void setParentCountFieldName(String parentCountFieldName) {
        _parentCountFieldName = parentCountFieldName;
    }

    public Long getCount() {
        return _count;
    }

    public void setCount(Long count) {
        _count = count;
    }
}
