package com.aperto.magkit.dialogs.fields;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;

import javax.annotation.Nullable;

/**
 * Enhance the text field functionality by showing an additional editorial label.
 *
 * @author Stefan Jahn
 * @since 05.12.14
 */
public class ExtendedTextField extends CustomField<String> {
    private static final long serialVersionUID = -6599211749794929718L;

    private final ExtendedTextFieldDefinition _definition;
    private final VerticalLayout _rootLayout = new VerticalLayout();
    private AbstractTextField _field;
    private Label _remainingLength = new Label();

    public ExtendedTextField(ExtendedTextFieldDefinition definition, AbstractTextField field) {
        _field = field;
        _definition = definition;
        setImmediate(true);
    }

    @Override
    public Component initContent() {
        _rootLayout.setSizeFull();
        _rootLayout.setSpacing(true);
        _rootLayout.setPrimaryStyleName("aperto-extended-textfield");

        _field.setImmediate(true);
        _field.setWidth(100, Sizeable.Unit.PERCENTAGE);
        _field.setNullRepresentation("");
        _field.setNullSettingAllowed(true);

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
    public void updateRemainingLength(@Nullable int inputValue, @Nullable int availableLength) {
        getRemainingLength().setValue(availableLength - inputValue + "/" + availableLength);
    }

    @Override
    public void setValue(String newValue) {
        _field.setValue(newValue);
    }

    @Override
    public String getValue() {
        return _field.getValue();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void setPropertyDataSource(Property newDataSource) {
        _field.setPropertyDataSource(newDataSource);
        super.setPropertyDataSource(newDataSource);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Property getPropertyDataSource() {
        return _field.getPropertyDataSource();
    }

    @Override
    public Class<? extends String> getType() {
        return String.class;
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
