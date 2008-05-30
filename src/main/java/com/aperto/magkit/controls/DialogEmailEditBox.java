package com.aperto.magkit.controls;

import com.aperto.webkit.utils.StringTools;
import info.magnolia.cms.gui.dialog.DialogEdit;
import info.magnolia.cms.util.AlertUtil;

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
        if (isValid) {
            isValid = StringTools.isValidEmailAddress(getValue());
            if (!isValid) {
                AlertUtil.setMessage("dialog.validation.email.wrongFormat");
            }
        }

        return isValid;
    }
}
