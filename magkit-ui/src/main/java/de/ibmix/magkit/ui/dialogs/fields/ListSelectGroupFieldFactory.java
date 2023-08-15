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

import com.vaadin.v7.data.Item;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.ListSelect;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.factory.OptionGroupFieldFactory;

import javax.inject.Inject;

/**
 * Factory for displaying options in a select list.
 *
 * @author frank.sommer
 * @since 21.10.14
 * @deprecated use new ui 6 field {@link info.magnolia.ui.field.factory.ListSelectFieldFactory}
 */
@Deprecated(since = "3.5.2")
public class ListSelectGroupFieldFactory extends OptionGroupFieldFactory<ListSelectFieldDefinition> {

    @Inject
    public ListSelectGroupFieldFactory(final ListSelectFieldDefinition definition, final Item relatedFieldItem, final UiContext uiContext, final I18NAuthoringSupport i18nAuthoringSupport, final ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport, componentProvider);
    }


    @Override
    protected AbstractSelect createSelectionField() {
        ListSelect listSelect = new ListSelect();
        listSelect.setRows(((ListSelectFieldDefinition) definition).getRows());
        return listSelect;
    }
}
