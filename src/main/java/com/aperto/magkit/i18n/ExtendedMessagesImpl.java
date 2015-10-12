package com.aperto.magkit.i18n;

import java.util.Locale;

import info.magnolia.cms.i18n.DefaultMessagesImpl;

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
