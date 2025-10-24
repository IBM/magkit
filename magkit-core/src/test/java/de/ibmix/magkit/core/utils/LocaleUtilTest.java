package de.ibmix.magkit.core.utils;

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


import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation;
import de.ibmix.magkit.test.cms.site.SiteMockUtils;
import de.ibmix.magkit.test.jcr.NodeMockUtils;
import info.magnolia.module.site.Site;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing LocaleUtil.
 *
 * @author wolf.bubenik.ibmic.de
 * @since 2024-01-03
 */
public class LocaleUtilTest {

    private Site _defaultSite;

    @Test
    public void getConfiguredLanguages() throws RepositoryException {
        assertEquals(0, LocaleUtil.getConfiguredLanguages().size());
        LocaleUtil.resetDefaultSiteLocals();
        I18nContentSupportStubbingOperation.stubLocales(Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN).of(_defaultSite.getI18n());
        assertEquals(3, LocaleUtil.getConfiguredLanguages().size());
        assertTrue(LocaleUtil.getConfiguredLanguages().contains("de"));
        assertTrue(LocaleUtil.getConfiguredLanguages().contains("en"));
        assertTrue(LocaleUtil.getConfiguredLanguages().contains("fr"));
    }

    @Test
    public void getSiteLocales() throws RepositoryException {
        assertEquals(0, LocaleUtil.getSiteLocales().size());

        LocaleUtil.resetDefaultSiteLocals();
        I18nContentSupportStubbingOperation.stubLocales(Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH).of(_defaultSite.getI18n());
        assertEquals(3, LocaleUtil.getSiteLocales().size());
    }

    @Test
    public void getDefaultSiteLocale() throws RepositoryException {
        assertEquals(Locale.ENGLISH, LocaleUtil.getDefaultSiteLocale());

        // this method does not return the default locale...
        I18nContentSupportStubbingOperation.stubDefaultLocale(Locale.FRENCH).of(_defaultSite.getI18n());
        assertEquals(Locale.ENGLISH, LocaleUtil.getDefaultSiteLocale());

        // ... but the fallback locale:
        I18nContentSupportStubbingOperation.stubFallbackLocale(Locale.FRENCH).of(_defaultSite.getI18n());
        assertEquals(Locale.FRENCH, LocaleUtil.getDefaultSiteLocale());
    }

    @Test
    public void determineLocaleFromContent() throws RepositoryException {
        Node page = null;
        assertEquals("en", LocaleUtil.determineLocaleFromContent(page));

        page = NodeMockUtils.mockNode("/it/fr/de/some/path");
        assertEquals("en", LocaleUtil.determineLocaleFromContent(page));

        I18nContentSupportStubbingOperation.stubFallbackLocale(Locale.FRENCH).of(_defaultSite.getI18n());
        assertEquals("fr", LocaleUtil.determineLocaleFromContent(page));

        LocaleUtil.resetDefaultSiteLocals();
        I18nContentSupportStubbingOperation.stubLocales(Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH).of(_defaultSite.getI18n());
        assertEquals("fr", LocaleUtil.determineLocaleFromContent(page));

        LocaleUtil.resetDefaultSiteLocals();
        I18nContentSupportStubbingOperation.stubLocales(Locale.GERMAN, Locale.ITALIAN, Locale.FRENCH).of(_defaultSite.getI18n());
        assertEquals("it", LocaleUtil.determineLocaleFromContent(page));
    }

    @Test
    public void determineLocaleLabelFromNodePath() throws RepositoryException {
        Node page = NodeMockUtils.mockNode("/it/fr/de/some/path");
        I18nContentSupportStubbingOperation.stubLocales(Locale.GERMAN, Locale.ITALIAN, Locale.FRENCH).of(_defaultSite.getI18n());
        assertEquals("italiano", LocaleUtil.determineLocaleLabelFromNodePath(page));
    }

    @Test
    public void getAvailableCountries() {
        assertEquals(251, LocaleUtil.getAvailableCountries().size());
        // verify that we removed doublets
        assertTrue(LocaleUtil.getAvailableCountries().size() < Locale.getAvailableLocales().length);
    }

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _defaultSite = SiteMockUtils.mockDefaultSite();
    }

    @AfterEach
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
        LocaleUtil.resetDefaultSiteLocals();
    }

}