package com.aperto.magkit.controller;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import com.aperto.magkit.beans.*;

/**
 * Validates contact form submissions.
 *
 * @author frank.sommer, 10.03.2008
 */
public class SearchValidator implements Validator {
    private static final Logger LOGGER = Logger.getLogger(SearchValidator.class);

    /**
     * Supports {@link com.aperto.degewo.mvc.commands.Contact}'s.
     */
    public boolean supports(Class clazz) {
        return Search.class.isAssignableFrom(clazz);
    }

    /**
     * Validates a set of fields in the given {@link com.aperto.degewo.mvc.commands.Contact}.
     * Whether the captcha gets validated depends on the _validateCaptcha field.
     * It is initially set to true but can be set to false for unit tests.
     */
    public void validate(Object target, Errors errors) {
        Search search = (Search) target;
        validateQuery(search, errors);
    }

    /**
     * Validates entered mail address.
     */
    private void validateQuery(Search search, Errors errors) {
        String query = search.getQ();
        if (StringUtils.isBlank(query)) {
            errors.rejectValue("q", "search.field.empty", "search.field.empty");
        } else if (query.trim().length() < 3) {
            errors.rejectValue("q", "search.field.short", "search.field.short");       
        }
    }
}
