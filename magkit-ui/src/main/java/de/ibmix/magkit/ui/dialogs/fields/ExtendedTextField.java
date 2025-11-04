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
 * Custom field adding an editorial remaining-length label to a wrapped {@link AbstractTextField}.
 * <p>
 * The label displays <code>currentUsed/available</code> characters. The available length is derived from the smaller
 * of configured max length and recommended length; if neither is positive the label is omitted. Value changes update
 * the label lazily (Vaadin {@link ValueChangeMode#LAZY}).
 * </p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Dynamic remaining length indicator.</li>
 *   <li>Transparent delegation of value handling to inner field.</li>
 *   <li>Focus delegation and consistent required/caption handling.</li>
 * </ul>
 *
 * <p>Usage preconditions: Constructed by {@link ExtendedTextFieldFactory} only when recommended length is set and no max length configured.</p>
 * <p>Thread-safety: Not thread-safe; UI component confined to Vaadin UI thread.</p>
 *
 * @author Janine.Kleessen
 * @since 2021-02-17
 */
@StyleSheet("extendedTextField.css")
public class ExtendedTextField extends CustomField<String> {

    private static final long serialVersionUID = -6599211749794929718L;

    private final ExtendedTextFieldDefinition _definition;
    private final AbstractTextField _textField;
    private final Label _remainingLength = new Label();
    private final int _availableLength;

    /**
     * Create extended field wrapper.
     * @param definition field definition supplying length constraints
     * @param innerComponent underlying Vaadin text component
     */
    public ExtendedTextField(final ExtendedTextFieldDefinition definition, final AbstractTextField innerComponent) {
        _definition = definition;
        _textField = innerComponent;
        _availableLength = determineLabelMaxLength();
    }

    /**
     * Determines the effective maximum for the remaining-length label from max/recommended lengths.
     * @return positive length or -1 if no label needed
     */
    protected int determineLabelMaxLength() {
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

    /**
     * Initialize Vaadin content structure and label setup.
     * @return root layout component
     */
    @Override
    public Component initContent() {
        _textField.setCaption(null);
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
     * Update remaining length label with new character counts.
     * @param inputValue current input length
     * @param availableLength total available length
     */
    public void updateRemainingLength(int inputValue, int availableLength) {
        getRemainingLength().setValue(availableLength - inputValue + "/" + availableLength);
    }

    /**
     * Delegate setting value to inner text field.
     * @param value new value
     */
    @Override
    protected void doSetValue(final String value) {
        _textField.setValue(value);
    }

    /**
     * @return current value from inner field
     */
    @Override
    public String getValue() {
        return _textField.getValue();
    }

    /**
     * @return empty value representation
     */
    @Override
    public String getEmptyValue() {
        return _textField.getEmptyValue();
    }

    /**
     * @return whether inner field is empty
     */
    @Override
    public boolean isEmpty() {
        return _textField.isEmpty();
    }

    /**
     * @return remaining length label component
     */
    public Label getRemainingLength() {
        return _remainingLength;
    }

    /**
     * Register value change listener delegating to inner field.
     * @param listener listener to register
     * @return registration handle
     */
    @Override
    public Registration addValueChangeListener(final ValueChangeListener<String> listener) {
        return _textField.addValueChangeListener(listener);
    }
}
