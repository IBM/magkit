package com.aperto.magkit.dialogs.fields;

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