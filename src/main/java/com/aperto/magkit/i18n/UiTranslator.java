package com.aperto.magkit.i18n;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
