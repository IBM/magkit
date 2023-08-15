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
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.event.FieldEvents;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Enhance the text field functionality by showing an additional editorial label.
 *
 * @author Stefan Jahn
 * @since 05.12.14
 * @deprecated use new ui 6 field {@link de.ibmix.magkit.ui.dialogs.m6.fields.ExtendedTextField}
 */
@Deprecated(since = "3.5.2")
@StyleSheet("extendedTextField.css")
public class ExtendedTextField extends CustomField<String> {
    private static final long serialVersionUID = -6599211749794929718L;
    public static final int FULL_WIDTH = 100;

    private final ExtendedTextFieldDefinition _definition;
    private final VerticalLayout _rootLayout = new VerticalLayout();
    private AbstractTextField _field;
    private Label _remainingLength = new Label();

    public ExtendedTextField(ExtendedTextFieldDefinition definition, AbstractTextField field) {
        _field = field;
        _definition = definition;
    }

    @Override
    public Component initContent() {
        _rootLayout.setSizeFull();
        _rootLayout.setSpacing(true);
        _rootLayout.setPrimaryStyleName("aperto-extended-textfield");

        _field.setWidth(FULL_WIDTH, Sizeable.Unit.PERCENTAGE);

        _rootLayout.addComponent(_field);
        configureLabel();

        return _rootLayout;
    }

    public void configureLabel() {
        final int availableLength = determineLabelMaxLength();

        // check the initial value of the text field
        int textLength = 0;
        if (_field.getValue() != null) {
            textLength = _field.getValue().length();
        }

        // initial editorial length label
        if (availableLength > 0) {
            _remainingLength.setValue(availableLength - textLength + "/" + availableLength);
        }

        // if no length is defined, no additional label will be shown
        if (availableLength > 0) {
            _remainingLength.setPrimaryStyleName("aperto-extended-textfield-label");
            _rootLayout.addComponent(_remainingLength);

            // change Listener, der das Label aktualisiert
            _field.addTextChangeListener(new FieldEvents.TextChangeListener() {
                private static final long serialVersionUID = 1337993932855486848L;

                @Override
                public void textChange(FieldEvents.TextChangeEvent event) {
                    // event.getText() is read asynchronously to decrease the server communication
                    updateRemainingLength(event.getText().length(), availableLength);
                }
            });
        }
    }

    /**
     * Determines the maximum value for the label.
     *
     * @return second value of the label
     */
    protected int determineLabelMaxLength() {
        final int maxLength = _definition.getMaxLength();
        final int recommendedLength = _definition.getRecommendedLength();
        final int availableLength;
        if (maxLength > 0 && recommendedLength > 0) {
            availableLength = Math.min(maxLength, recommendedLength);
        } else if (maxLength > 0) {
            availableLength = maxLength;
        } else if (recommendedLength > 0) {
            availableLength = recommendedLength;
        } else {
            availableLength = -1;
        }
        return availableLength;
    }

    /**
     * Update the label.
     */
    public void updateRemainingLength(int inputValue, int availableLength) {
        getRemainingLength().setValue(availableLength - inputValue + "/" + availableLength);
    }

    @Override
    public void setValue(String newValue) {
        _field.setValue(newValue);
    }

    @Override
    public Class<? extends String> getType() {
        return String.class;
    }

    @Override
    public String getValue() {
        return _field.getValue();
    }

    /**
     * Set propertyDatasource.
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        _field.setPropertyDataSource(newDataSource);
        super.setPropertyDataSource(newDataSource);
    }

    @Override
    public Property getPropertyDataSource() {
        return _field.getPropertyDataSource();
    }

    public Label getRemainingLength() {
        return _remainingLength;
    }

    public void setRemainingLength(Label remainingLength) {
        _remainingLength = remainingLength;
    }

    public AbstractTextField getField() {
        return _field;
    }

}