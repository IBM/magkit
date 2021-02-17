package com.aperto.magkit.dialogs.m6.fields;


import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Enhance the text field functionality by showing an additional editorial label.
 *
 * @author Janine.Kleessen
 * @since 17.02.2021
 */
@StyleSheet("extendedTextField.css")
public class ExtendedTextField extends CustomField<String> {
    private static final long serialVersionUID = -6599211749794929718L;
    public static final int FULL_WIDTH = 100;

    private final ExtendedTextFieldDefinition _definition;
    private final VerticalLayout _rootLayout = new VerticalLayout();
    private final AbstractTextField _field;
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
            _remainingLength.setWidth(FULL_WIDTH, Unit.PERCENTAGE);
            _rootLayout.addComponent(_remainingLength);

            // change Listener, der das Label aktualisiert
            _field.addValueChangeListener(event -> updateRemainingLength(event.getValue().length(), availableLength));
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
    public String getValue() {
        return null;
    }

    @Override
    protected void doSetValue(String s) {
        _field.setValue(s);
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
