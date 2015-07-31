package com.aperto.magkit.dialogs.fields;

import info.magnolia.ui.form.config.MultiValueFieldBuilder;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;

/**
 * Builder for the {@link SpecificMultiField} instance.
 *
 * @author noreply@aperto.com
 */
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
