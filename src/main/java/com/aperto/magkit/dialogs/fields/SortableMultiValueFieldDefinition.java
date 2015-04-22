package com.aperto.magkit.dialogs.fields;

import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.transformer.multi.MultiValueTransformer;

/**
 * Definition class for the sortable MultiValueField.
 * There are two new properties (maxComponents and sortable).
 *
 * @author Stefan Jahn
 * @since 02.12.14
 */
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