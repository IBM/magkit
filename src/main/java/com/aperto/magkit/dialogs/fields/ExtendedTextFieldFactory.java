package com.aperto.magkit.dialogs.fields;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;
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
    public ExtendedTextFieldFactory(final ExtendedTextFieldDefinition definition, final Item relatedFieldItem, final UiContext uiContext, final I18NAuthoringSupport i18nAuthoringSupport) {
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
