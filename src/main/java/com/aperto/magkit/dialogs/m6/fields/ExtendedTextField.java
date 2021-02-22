package com.aperto.magkit.dialogs.m6.fields;


import com.vaadin.annotations.StyleSheet;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import java.util.Optional;

/**
 * Enhance the text field functionality by showing an additional editorial label.
 *
 * @author Janine.Kleessen
 * @since 17.02.2021
 */
@StyleSheet("extendedTextField.css")
public class ExtendedTextField extends CustomField<String> {

    private static final long serialVersionUID = -6599211749794929718L;

    private final ExtendedTextFieldDefinition _definition;
    private final AbstractTextField _textField;
    private final Label _remainingLength = new Label();
    private final int _availableLength;

    public ExtendedTextField(final ExtendedTextFieldDefinition definition, final AbstractTextField innerComponent) {
        _definition = definition;
        _textField = innerComponent;
        _availableLength = determineLabelMaxLength();
    }

    /**
     * Determines the maximum value for the label.
     *
     * @return second value of the label
     */
    private int determineLabelMaxLength() {
        int max = _definition.getMaxLength();
        int recommended = _definition.getRecommendedLength();
        int available;

        if (max > 0 && recommended > 0) {
            available = Math.min(max, recommended);
        } else if (max > 0) {
            available = max;
        } else if (recommended > 0) {
            available = recommended;
        } else {
            available = -1;
        }

        return available;
    }

    @Override
    public Component initContent() {
        _textField.setCaption(null);
        _textField.setPrimaryStyleName("v-textfield");
        _textField.setRequiredIndicatorVisible(false);
        _textField.setWidth(100, Unit.PERCENTAGE);
        _textField.setValueChangeMode(ValueChangeMode.LAZY);
        _textField.addValueChangeListener(event -> setValue(event.getValue()));

        VerticalLayout root = new VerticalLayout();
        root.setPrimaryStyleName("aperto-extended-textfield");
        root.setMargin(false);
        root.setSizeFull();
        root.setSpacing(false);
        root.addComponent(_textField);

        // check the initial value of the text field
        int textLength = Optional.ofNullable(_textField.getValue())
            .map(String::length)
            .orElse(0);

        // initial editorial length label: if no length is defined, no additional label will be shown
        if (_availableLength > 0) {
            _remainingLength.setValue(_availableLength - textLength + "/" + _availableLength);
            _remainingLength.setPrimaryStyleName("aperto-extended-textfield-label");
            _remainingLength.setCaption(null);

            _textField.addValueChangeListener(event -> updateRemainingLength(event.getValue().length(), _availableLength));

            root.addComponent(_remainingLength);
        }

        setValue(_textField.getValue());
        setFocusDelegate(_textField);

        return root;
    }

    /**
     * Update the label.
     */
    public void updateRemainingLength(int inputValue, int availableLength) {
        getRemainingLength().setValue(availableLength - inputValue + "/" + availableLength);
    }

    @Override
    protected void doSetValue(final String value) {
        _textField.setValue(value);
    }

    @Override
    public String getValue() {
        return _textField.getValue();
    }

    @Override
    public String getEmptyValue() {
        return _textField.getEmptyValue();
    }

    @Override
    public boolean isEmpty() {
        return _textField.isEmpty();
    }

    public Label getRemainingLength() {
        return _remainingLength;
    }

    @Override
    public Registration addValueChangeListener(final ValueChangeListener<String> listener) {
        return _textField.addValueChangeListener(listener);
    }
}
