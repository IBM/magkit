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
import info.magnolia.i18nsystem.TranslationService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubLocale;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link UiTranslator} verifying delegation of locale resolution to {@link I18nContentSupport}.
 * Covers constructor wiring, dynamic locale retrieval, null locale scenario and independence of multiple instances.
 *
 * @author wolf.bubenik.ibmic.de
 * @since 2025-10-31
 */
public class UiTranslatorTest {

    /**
     * Verifies that the locale provider used by {@link UiTranslator} returns the initial content locale.
     */
    @Test
    public void testLocaleProviderInitialLocale() throws Exception {
        I18nContentSupport i18nContentSupport = mockI18nContentSupport(stubLocale(Locale.GERMAN));
        TranslationService translationService = mock(TranslationService.class);
        UiTranslator translator = new UiTranslator(translationService, i18nContentSupport);
        LocaleProvider provider = extractLocaleProvider(translator);
        assertEquals(Locale.GERMAN, provider.getLocale());
    }

    /**
     * Verifies that changing the stubbed locale on {@link I18nContentSupport} is reflected on subsequent provider calls.
     */
    @Test
    public void testLocaleProviderDynamicChange() throws Exception {
        I18nContentSupport i18nContentSupport = mockI18nContentSupport(stubLocale(Locale.ENGLISH));
        TranslationService translationService = mock(TranslationService.class);
        UiTranslator translator = new UiTranslator(translationService, i18nContentSupport);
        LocaleProvider provider = extractLocaleProvider(translator);
        assertEquals(Locale.ENGLISH, provider.getLocale());
        stubLocale(Locale.FRENCH).of(i18nContentSupport);
        assertEquals(Locale.FRENCH, provider.getLocale());
        stubLocale(Locale.ITALIAN).of(i18nContentSupport);
        assertEquals(Locale.ITALIAN, provider.getLocale());
    }

    /**
     * Verifies that a null locale from {@link I18nContentSupport} is passed through (Magnolia fallback applies elsewhere).
     */
    @Test
    public void testLocaleProviderNullLocale() throws Exception {
        I18nContentSupport i18nContentSupport = mockI18nContentSupport();
        TranslationService translationService = mock(TranslationService.class);
        UiTranslator translator = new UiTranslator(translationService, i18nContentSupport);
        LocaleProvider provider = extractLocaleProvider(translator);
        assertNull(provider.getLocale());
    }

    /**
     * Verifies independence of multiple UiTranslator instances with distinct I18nContentSupport mocks.
     */
    @Test
    public void testMultipleTranslatorIndependence() throws Exception {
        I18nContentSupport firstSupport = mockI18nContentSupport(stubLocale(Locale.JAPANESE));
        I18nContentSupport secondSupport = mockI18nContentSupport(stubLocale(Locale.KOREAN));
        TranslationService translationService = mock(TranslationService.class);
        UiTranslator firstTranslator = new UiTranslator(translationService, firstSupport);
        UiTranslator secondTranslator = new UiTranslator(translationService, secondSupport);
        assertEquals(Locale.JAPANESE, extractLocaleProvider(firstTranslator).getLocale());
        assertEquals(Locale.KOREAN, extractLocaleProvider(secondTranslator).getLocale());
        stubLocale(Locale.CHINESE).of(firstSupport);
        stubLocale(Locale.TAIWAN).of(secondSupport);
        assertEquals(Locale.CHINESE, extractLocaleProvider(firstTranslator).getLocale());
        assertEquals(Locale.TAIWAN, extractLocaleProvider(secondTranslator).getLocale());
    }

    /**
     * Extracts the private locale provider from the SimpleTranslator superclass via reflection for assertion.
     *
     * @param translator translator instance under test
     * @return underlying LocaleProvider
     * @throws Exception reflection access failures
     */
    private LocaleProvider extractLocaleProvider(UiTranslator translator) throws Exception {
        Field localeProviderField = translator.getClass().getSuperclass().getDeclaredField("localeProvider");
        localeProviderField.setAccessible(true);
        return (LocaleProvider) localeProviderField.get(translator);
    }
}
