package de.ibmix.magkit.ui.i18n;

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
 * Provides translation of Magnolia UI/content messages using the current content locale instead of the author/editor locale.
 * <p>
 * Magnolia's {@link SimpleTranslator} resolves messages via a {@link LocaleProvider} typically bound to the editor language.
 * This class is a lightweight drop-in replacement where translation must reflect the content's locale context (e.g. dialog labels
 * depending on a node's language).
 * </p>
 * <p><strong>Main functionalities and key features:</strong></p>
 * <ul>
 *   <li>Delegates locale resolution dynamically to {@link I18nContentSupport#getLocale()}.</li>
 *   <li>Retains all formatting and fallback behavior of {@link SimpleTranslator}.</li>
 *   <li>Stateless design: no additional fields or caching.</li>
 *   <li>Transparent replacement wherever a {@code SimpleTranslator} is injected.</li>
 * </ul>
 * <p><strong>Usage preconditions:</strong> Requires an active Magnolia context with properly configured {@link I18nContentSupport}.
 * Inject this class where a {@code SimpleTranslator} would normally be used if content-based locale switching is desired.</p>
 * <p><strong>Side effects:</strong> None. Locale is resolved per invocation; no mutation of global state.</p>
 * <p><strong>Null and error handling:</strong> Assumes {@link I18nContentSupport#getLocale()} returns a non-null {@link Locale}. If it returns
 * {@code null}, Magnolia's underlying translation service fallback applies (typically default locale).</p>
 * <p><strong>Thread-safety:</strong> Stateless and safe for concurrent use in request/UI threads as with {@link SimpleTranslator}.</p>
 * <p><strong>Usage example:</strong></p>
 * <pre>{@code
 *   UiTranslator uiTranslator;
 *   String label = uiTranslator.translate("my.module.key");
 * }</pre>
 *
 * @author frank.sommer
 * @since 2016-05-30
 */
public class UiTranslator extends SimpleTranslator {

    /**
     * Constructs a UI translator bound to the current content locale.
     *
     * @param translationService Magnolia translation service used for resolving i18n keys.
     * @param i18nContentSupport content support providing the current content {@link Locale}.
     */
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
