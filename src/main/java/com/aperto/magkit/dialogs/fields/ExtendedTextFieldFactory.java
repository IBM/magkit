package com.aperto.magkit.dialogs.fields;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for {@link ExtendedTextField}.
 *
 * @author Stefan Jahn
 * @since 21.11.14
 */
public class ExtendedTextFieldFactory extends AbstractFieldFactory<ExtendedTextFieldDefinition, String> {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExtendedTextFieldFactory.class);

    public ExtendedTextFieldFactory(ExtendedTextFieldDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    /**
     * Wraps the defined text field as extended text field.
     */
    @Override
    protected Field<String> createFieldComponent() {
        // create a text area, if more than one row defined
        final AbstractTextField textField;
        if (definition.getRows() > 1) {
            TextArea area = new TextArea();
            area.setRows(definition.getRows());
            textField = area;
        } else {
            textField = new TextField();
        }
        if (definition.getMaxLength() > 0) {
            textField.setMaxLength(definition.getMaxLength());
        }

        return new ExtendedTextField(definition, textField);
    }
}