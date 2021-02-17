package com.aperto.magkit.dialogs.m6.fields;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.field.factory.TextFieldFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Factory for {@link ExtendedTextField}.
 *
 * @author Janine.Kleessen
 * @since 17.02.2021
 */
public class ExtendedTextFieldFactory extends TextFieldFactory {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExtendedTextFieldFactory.class);

    @Inject
    public ExtendedTextFieldFactory(final ExtendedTextFieldDefinition definition, ComponentProvider componentProvider) {
        super(definition, componentProvider);
    }

    /**
     * Wraps the defined text field as extended text field.
     */
    @Override
    public Component createFieldComponent() {
        Component fieldComponent = super.createFieldComponent();

        if (definition instanceof ExtendedTextFieldDefinition) {
            if (definition.getMaxLength() < 1 && ((ExtendedTextFieldDefinition) definition).getRecommendedLength() > 0) {
                fieldComponent = new ExtendedTextField((ExtendedTextFieldDefinition) definition, (AbstractTextField) fieldComponent);
            }
        }

        return fieldComponent;
    }
}
