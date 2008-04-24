package com.aperto.magkit.i18n;

import com.aperto.magkit.MagKitTest;
import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.context.MgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;
import javax.servlet.jsp.PageContext;
import java.util.Locale;

/**
 * Test of the i18n handle content support.
 *
 * @author frank.sommer (24.04.2008)
 */
public class HandleI18nContentSupportTest extends MagKitTest {
    @Test
    public void testLocalizedHandleNode() {
        MgnlContext.getAggregationState().setCurrentURI("/content/de/testpage.html");
        HandleI18nContentSupport contentSupport = (HandleI18nContentSupport) FactoryUtil.newInstance(HandleI18nContentSupport.class);
        LocaleDefinition definition = new LocaleDefinition();
        definition.setLocale(new Locale("de"));
        definition.setEnabled(true);
        contentSupport.addLocale(definition);
        Locale locale = contentSupport.determineLocale();
        assertThat(locale.getLanguage(), is("de"));
    }

    @Test
    public void testLocalizedHandleSite() {
        MgnlContext.getAggregationState().setCurrentURI("de.html");
        HandleI18nContentSupport contentSupport = (HandleI18nContentSupport) FactoryUtil.newInstance(HandleI18nContentSupport.class);
        LocaleDefinition definition = new LocaleDefinition();
        definition.setLocale(new Locale("de"));
        definition.setEnabled(true);
        contentSupport.addLocale(definition);
        Locale locale = contentSupport.determineLocale();
        assertThat(locale.getLanguage(), is("de"));
    }

    @Test
    public void testUnlocalizedHandle() {
        MgnlContext.getAggregationState().setCurrentURI("/content/den/testpage.html");        
        HandleI18nContentSupport contentSupport = (HandleI18nContentSupport) FactoryUtil.newInstance(HandleI18nContentSupport.class);
        LocaleDefinition definition = new LocaleDefinition();
        definition.setLocale(new Locale("de"));
        definition.setEnabled(true);
        contentSupport.addLocale(definition);
        Locale locale = contentSupport.determineLocale();
        assertThat(locale.getLanguage(), is("en"));
    }

    @Test
    public void testLocalizedHandleWithCountry() {
        MgnlContext.getAggregationState().setCurrentURI("/content/de_DE/testpage.html");
        HandleI18nContentSupport contentSupport = (HandleI18nContentSupport) FactoryUtil.newInstance(HandleI18nContentSupport.class);
        LocaleDefinition definition = new LocaleDefinition();
        definition.setLocale(new Locale("de", "DE"));
        definition.setEnabled(true);
        contentSupport.addLocale(definition);
        Locale locale = contentSupport.determineLocale();
        assertThat(locale.getLanguage(), is("de"));
    }

    @Before
    public void createMgnlContext() {
        createPageContext();
    }

    @Override
    protected PageContext createPageContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();

        initMgnlWebContext(request, response, httpSession.getServletContext());
        MgnlContext.getAggregationState().setCharacterEncoding("utf-8");

        return new MockPageContext(new MockServletConfig(), request, response);
    }
}