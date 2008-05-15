package com.aperto.magkit.controls;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import com.aperto.webkit.utils.StringTools;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.gui.dialog.DialogEdit;
import info.magnolia.cms.gui.dialog.DialogControl;
import info.magnolia.cms.gui.dialog.DialogControlImpl;

import java.util.Iterator;

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
        boolean isValid = true;

        // ********************************
        // * BEGIN: This is just copied from original validate
        // ********************************
        if (isRequired()) {
            if (StringUtils.isEmpty(getValue()) && getValues().size() == 0) {
                String name = getMessage(getLabel());
                AlertUtil.setMessage(getMessage("dialog.validation.required", new Object[]{name}));
                isValid = false;
            }
        }
        for (Object o : getSubs()) {
            DialogControl sub = (DialogControl) o;
            if (sub instanceof DialogControlImpl) {
                if (!((DialogControlImpl) sub).validate()) {
                    isValid = false;
                }
            }
        }
        // ********************************
        // * END: This is just copied from original validate
        // ********************************

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
