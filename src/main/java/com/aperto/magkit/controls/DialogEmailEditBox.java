package com.aperto.magkit.controls;

import com.aperto.webkit.utils.StringTools;
import info.magnolia.cms.gui.dialog.DialogEdit;
import org.apache.commons.lang.StringUtils;

/**
 * Dialog control for validating the input as email address.
 *
 * @author rainer.blumenthal, frank.sommer
 *         Date: 08.04.2008
 *         Time: 11:49:01
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
        if (isValid && StringUtils.isNotEmpty(value)) {
            isValid = StringTools.isValidEmailAddress(value);
            if (!isValid) {
                setValidationMessage("dialog.validation.email.wrongFormat");
            }
        }

        return isValid;
    }
}
