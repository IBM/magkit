package com.aperto.magkit.controls;

import java.net.MalformedURLException;
import java.net.URL;

import info.magnolia.cms.gui.dialog.DialogEdit;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.contains;
import org.apache.commons.lang.StringUtils;

/**
 * Dialog control that ensures the input string is a valid url.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class DialogUrlEditBox extends DialogEdit {

    /**
     * Overriden methode for validating.
     */
    @Override
    public boolean validate() {
        boolean isValid = super.validate();
        // validate whether the given string is valid url
        String value = getValue();
        if (isValid && isNotBlank(value)) {
            try {
                new URL(value);
            } catch (MalformedURLException e) {
                isValid = false;
                setValidationMessage("dialog.validation.url.wrongFormat");
            }
            for (int i = 0; i < value.length(); i++) {
            	if (Character.isWhitespace(value.charAt(i))) {
                    isValid = false;
                    setValidationMessage("dialog.validation.url.wrongFormat");
                    break;
                }
            }
        }
        return isValid;
    }
}