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

import de.ibmix.magkit.ui.dialogs.m6.fields.SpecificMultiFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.transformer.multi.MultiValueTransformer;

/**
 * Definition class for the sortable MultiValueField.
 * There are two new properties (maxComponents and sortable).
 *
 * @author Stefan Jahn
 * @since 02.12.14
 * @deprecated use new ui 6 field {@link SpecificMultiFieldDefinition}
 */
@Deprecated(since = "3.5.2")
public class SortableMultiValueFieldDefinition extends MultiValueFieldDefinition {

    private Long _maxComponents = Long.MAX_VALUE;
    private boolean _sortable = true;

    public SortableMultiValueFieldDefinition() {
        setTransformerClass(MultiValueTransformer.class);
    }

    public Long getMaxComponents() {
        return _maxComponents;
    }

    public void setMaxComponents(final Long maxComponents) {
        _maxComponents = maxComponents;
    }

    public boolean getSortable() {
        return _sortable;
    }

    public void setSortable(final boolean sortable) {
        _sortable = sortable;
    }
}
