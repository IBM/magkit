package com.aperto.magkit.i18n;

import info.magnolia.cms.i18n.DefaultMessagesImpl;

import java.util.Locale;

/**
 * Messages class only to get protected constructor.
 *
 * @author diana.racho  (Aperto AG)
 */
public class ExtendedMessagesImpl extends DefaultMessagesImpl {
    protected ExtendedMessagesImpl(String basename, Locale locale) {
        super(basename, locale);
    }
}
