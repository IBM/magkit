package com.aperto.magkit.dialogs.fields;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Factory for {@link ExtendedTextField}.
 *
 * @author Stefan Jahn
 * @since 21.11.14
 */
public class ExtendedTextFieldFactory extends AbstractFieldFactory<ExtendedTextFieldDefinition, String> {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExtendedTextFieldFactory.class);

    @Inject
    public ExtendedTextFieldFactory(ExtendedTextFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
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