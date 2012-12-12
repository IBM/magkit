package com.aperto.magkit.controls;

import info.magnolia.cms.gui.dialog.DialogEdit;
import org.apache.commons.validator.routines.EmailValidator;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Dialog control for validating the input as email address.
 *
 * @author rainer.blumenthal, frank.sommer
 * @since 08.04.2008
 */
public class DialogEmailEditBox extends DialogEdit {

    /**
     * Overriden methode for validating.
     */
    @Override
    public boolean validate() {
        boolean isValid = super.validate();

        //validate whether the given email is valid
        String value = getValue();
        if (isValid && isNotEmpty(value)) {

            isValid = EmailValidator.getInstance().isValid(value);
            if (!isValid) {
                setValidationMessage("dialog.validation.email.wrongFormat");
            }
        }

        return isValid;
    }
}
