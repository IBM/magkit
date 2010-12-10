package com.aperto.magkit.i18n;

import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubCharacterEncoding;
import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubCurrentUri;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAggregationState;
import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

/**
 * Test of the i18n handle content support.
 *
 * @author frank.sommer (24.04.2008)
 */
public class HandleI18nContentSupportTest {
    @Test
    public void testLocalizedHandleNode() {
        mockAggregationStateWith("utf-8", "/content/de/testpage.html");
        HandleI18nContentSupport contentSupport = createI18nContentSupport(new Locale("de"));
        Locale locale = contentSupport.determineLocale();
        assertThat(locale.getLanguage(), is("de"));
    }

    @Test
    public void testLocalizedHandleSite() {
        mockAggregationStateWith("utf-8", "/content/de/testpage.html");
        MgnlContext.getAggregationState().setCurrentURI("de.html");
        HandleI18nContentSupport contentSupport = createI18nContentSupport(new Locale("de"));
        Locale locale = contentSupport.determineLocale();
        assertThat(locale.getLanguage(), is("de"));
    }

    @Test
    public void testLocalizedHandleSiteWithMandant() {
        mockAggregationStateWith("utf-8", "de_mandant.html");
        MgnlContext.getAggregationState().setCurrentURI("de_mandant.html");
        HandleI18nContentSupport contentSupport = createI18nContentSupport(new Locale("de"));
        Locale locale = contentSupport.determineLocale();
        assertThat(locale.getLanguage(), is("de"));
    }

    @Test
    public void testUnlocalizedHandle() {
        mockAggregationStateWith("utf-8", "/content/den/testpage.html");
        HandleI18nContentSupport contentSupport = createI18nContentSupport(new Locale("de"));
        Locale locale = contentSupport.determineLocale();
        assertThat(locale.getLanguage(), is("en"));
    }

    @Test
    public void testLocalizedHandleWithCountry() {
        mockAggregationStateWith("utf-8", "/content/de_DE/testpage.html");
        HandleI18nContentSupport contentSupport = createI18nContentSupport(new Locale("de", "DE"));
        Locale locale = contentSupport.determineLocale();
        assertThat(locale.getLanguage(), is("de"));
    }

    @Test
    public void testLocalizedHandleWithCountryWithMandant() {
        mockAggregationStateWith("utf-8", "/content/de_DE_mandant/testpage.html");
        HandleI18nContentSupport contentSupport = createI18nContentSupport(new Locale("de", "DE"));
        Locale locale = contentSupport.determineLocale();
        assertThat(locale.getLanguage(), is("de"));
    }

    @Before
    public void createMgnlContext() {
        cleanContext();
    }

    protected void mockAggregationStateWith(String encoding, String currentUri) {
        mockAggregationState(
            stubCharacterEncoding(encoding),
            stubCurrentUri(currentUri)
        );
    }

    private HandleI18nContentSupport createI18nContentSupport(Locale locale) {
        HandleI18nContentSupport contentSupport = (HandleI18nContentSupport) FactoryUtil.newInstance(HandleI18nContentSupport.class);
        LocaleDefinition definition = new LocaleDefinition();
        definition.setLocale(locale);
        definition.setEnabled(true);
        contentSupport.addLocale(definition);
        return contentSupport;
    }
}