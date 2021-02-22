package com.aperto.magkit.dialogs.m6.fields;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.field.factory.TextFieldFactory;

import javax.inject.Inject;

/**
 * Factory for {@link ExtendedTextField}.
 *
 * @author Janine.Kleessen
 * @since 17.02.2021
 */
public class ExtendedTextFieldFactory extends TextFieldFactory {

    private final ExtendedTextFieldDefinition _definition;

    @Inject
    public ExtendedTextFieldFactory(final ExtendedTextFieldDefinition definition, ComponentProvider componentProvider) {
        super(definition, componentProvider);
        _definition = definition;
    }

    /**
     * Wraps the defined text field as extended text field.
     */
    @Override
    public Component createFieldComponent() {
        Component fieldComponent = super.createFieldComponent();

        if (_definition.getMaxLength() < 1 && _definition.getRecommendedLength() > 0) {
            fieldComponent = new ExtendedTextField(_definition, (AbstractTextField) fieldComponent);
        }

        return fieldComponent;
    }
}
