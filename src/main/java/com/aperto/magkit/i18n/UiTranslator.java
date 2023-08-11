package com.aperto.magkit.i18n;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Use the ui translator for user ui i18n instead of the simple translator.
 * Simple translator uses the editor language instead the content locale.
 *
 * @author frank.sommer
 * @since 30.05.2016
 */
public class UiTranslator extends SimpleTranslator {

    @Inject
    public UiTranslator(final TranslationService translationService, final I18nContentSupport i18nContentSupport) {
        super(translationService, new LocaleProvider() {
            @Override
            public Locale getLocale() {
                return i18nContentSupport.getLocale();
            }
        });
    }
}
