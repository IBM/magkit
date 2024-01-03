package de.ibmix.magkit.core.utils;

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation;
import de.ibmix.magkit.test.cms.site.SiteMockUtils;
import de.ibmix.magkit.test.jcr.NodeMockUtils;
import info.magnolia.module.site.Site;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

public class LocaleUtilTest {

    private Site _defaultSite;

    @Test
    public void getConfiguredLanguages() throws RepositoryException {
        assertThat(LocaleUtil.getConfiguredLanguages().size(), is(0));
        LocaleUtil.resetDefaultSiteLocals();
        I18nContentSupportStubbingOperation.stubLocales(Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN).of(_defaultSite.getI18n());
        assertThat(LocaleUtil.getConfiguredLanguages().size(), is(3));
        assertThat(LocaleUtil.getConfiguredLanguages().contains("de"), is(true));
        assertThat(LocaleUtil.getConfiguredLanguages().contains("en"), is(true));
        assertThat(LocaleUtil.getConfiguredLanguages().contains("fr"), is(true));
    }

    @Test
    public void getSiteLocales() throws RepositoryException {
        assertThat(LocaleUtil.getSiteLocales().size(), is(0));

        LocaleUtil.resetDefaultSiteLocals();
        I18nContentSupportStubbingOperation.stubLocales(Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH).of(_defaultSite.getI18n());
        assertThat(LocaleUtil.getSiteLocales().size(), is(3));
    }

    @Test
    public void getDefaultSiteLocale() throws RepositoryException {
        assertThat(LocaleUtil.getDefaultSiteLocale(), is(Locale.ENGLISH));

        // this method does not return the default locale...
        I18nContentSupportStubbingOperation.stubDefaultLocale(Locale.FRENCH).of(_defaultSite.getI18n());
        assertThat(LocaleUtil.getDefaultSiteLocale(), is(Locale.ENGLISH));

        // ... but the fallback locale:
        I18nContentSupportStubbingOperation.stubFallbackLocale(Locale.FRENCH).of(_defaultSite.getI18n());
        assertThat(LocaleUtil.getDefaultSiteLocale(), is(Locale.FRENCH));
    }

    @Test
    public void determineLocaleFromContent() throws RepositoryException {
        Node page = null;
        assertThat(LocaleUtil.determineLocaleFromContent(page), is("en"));

        page = NodeMockUtils.mockNode("/it/fr/de/some/path");
        assertThat(LocaleUtil.determineLocaleFromContent(page), is("en"));

        I18nContentSupportStubbingOperation.stubFallbackLocale(Locale.FRENCH).of(_defaultSite.getI18n());
        assertThat(LocaleUtil.determineLocaleFromContent(page), is("fr"));

        LocaleUtil.resetDefaultSiteLocals();
        I18nContentSupportStubbingOperation.stubLocales(Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH).of(_defaultSite.getI18n());
        assertThat(LocaleUtil.determineLocaleFromContent(page), is("fr"));

        LocaleUtil.resetDefaultSiteLocals();
        I18nContentSupportStubbingOperation.stubLocales(Locale.GERMAN, Locale.ITALIAN, Locale.FRENCH).of(_defaultSite.getI18n());
        assertThat(LocaleUtil.determineLocaleFromContent(page), is("it"));
    }

    @Test
    public void determineLocaleLabelFromNodePath() throws RepositoryException {
        Node page = NodeMockUtils.mockNode("/it/fr/de/some/path");
        I18nContentSupportStubbingOperation.stubLocales(Locale.GERMAN, Locale.ITALIAN, Locale.FRENCH).of(_defaultSite.getI18n());
        assertThat(LocaleUtil.determineLocaleLabelFromNodePath(page), is("italiano"));
    }

    @Test
    public void getAvailableCountries() {
        assertThat(LocaleUtil.getAvailableCountries().size(), is(251));
        // verify that we removed doublets
        assertTrue(LocaleUtil.getAvailableCountries().size() < Locale.getAvailableLocales().length);
    }

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _defaultSite = SiteMockUtils.mockDefaultSite();
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
        LocaleUtil.resetDefaultSiteLocals();
    }

}