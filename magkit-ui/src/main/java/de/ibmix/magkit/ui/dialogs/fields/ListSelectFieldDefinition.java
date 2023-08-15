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

import info.magnolia.i18nsystem.I18nText;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;

import java.util.ArrayList;
import java.util.List;

/**
 * List select field definition as alternative for an option group.
 *
 * @author frank.sommer
 * @since 21.10.14
 * @deprecated use new ui 6 field {@link info.magnolia.ui.field.ListSelectFieldDefinition}
 */
@Deprecated(since = "3.5.2")
public class ListSelectFieldDefinition extends OptionGroupFieldDefinition {
    private static final int DEF_ROWS = 6;
    protected static final String EMPTY_VALUE = "---";

    private int _rows = DEF_ROWS;
    private String _emptySelectionLabel = "listSelect.emptySelection.label";

    @SuppressWarnings({"unchecked", "RedundantCast"})
    public ListSelectFieldDefinition() {
        setTransformerClass((Class<? extends Transformer<?>>) (Object) ListSelectTransformer.class);
    }

    public int getRows() {
        return _rows;
    }

    public void setRows(final int rows) {
        _rows = rows;
    }

    @I18nText
    public String getEmptySelectionLabel() {
        return _emptySelectionLabel;
    }

    public void setEmptySelectionLabel(final String emptySelectionLabel) {
        _emptySelectionLabel = emptySelectionLabel;
    }

    @Override
    public List<SelectFieldOptionDefinition> getOptions() {
        List<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();
        if (!isRequired()) {
            SelectFieldOptionDefinition emptyOption = new SelectFieldOptionDefinition();
            emptyOption.setLabel(getEmptySelectionLabel());
            emptyOption.setValue(EMPTY_VALUE);
            options.add(emptyOption);
        }
        options.addAll(super.getOptions());
        return options;
    }
}
