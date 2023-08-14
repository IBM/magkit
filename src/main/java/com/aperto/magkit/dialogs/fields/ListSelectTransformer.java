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

import com.vaadin.v7.data.Item;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.ListToSetTransformer;

import java.util.Collection;

/**
 * Transformer filtered empty selection items.
 *
 * @param <T> type of value
 * @author frank.sommer
 * @since 04.11.14
 * @deprecated use new ui 6 field
 */
@Deprecated(since = "3.5.2")
public class ListSelectTransformer<T> extends ListToSetTransformer<T> {

    public ListSelectTransformer(final Item relatedFormItem, final ConfiguredFieldDefinition definition, final Class<T> type, I18NAuthoringSupport i18nAuthoringSupport) {
        super(relatedFormItem, definition, type, i18nAuthoringSupport);
    }

    @Override
    public void writeToItem(T newValue) {
        // remove the empty selection values
        if (newValue instanceof Collection) {
            ((Collection) newValue).remove(ListSelectFieldDefinition.EMPTY_VALUE);
        }
        super.writeToItem(newValue);
    }
}
