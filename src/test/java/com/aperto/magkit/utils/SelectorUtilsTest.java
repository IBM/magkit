package com.aperto.magkit.utils;

import info.magnolia.context.WebContext;
import org.junit.Before;
import org.junit.Test;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.utils.SelectorUtils.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test of the resource utils.
 *
 * @author frank.sommer (11.12.2008)
 * @see SelectorUtils
 */
public class SelectorUtilsTest {
    @Test
    public void retrieveActivePageWithNoValue() {
        WebContext webContext = mockWebContext();
        when(webContext.getAttribute(SELECTOR_PAGING)).thenReturn(null);
        assertThat(SelectorUtils.retrieveActivePage(), is(DEF_PAGE));
    }

    @Test
    public void retrieveActivePageWithLetterValue() {
        WebContext webContext = mockWebContext();
        when(webContext.getAttribute(SELECTOR_PAGING)).thenReturn("abc");
        assertThat(SelectorUtils.retrieveActivePage(), is(DEF_PAGE));
    }

    @Test
    public void retrieveActivePageWithInvalidValue() {
        WebContext webContext = mockWebContext();
        when(webContext.getAttribute(SELECTOR_PAGING)).thenReturn("-5");
        assertThat(SelectorUtils.retrieveActivePage(), is(DEF_PAGE));
    }

    @Test
    public void retrieveActivePageWithValidValue() {
        WebContext webContext = mockWebContext();
        when(webContext.getAttribute(SELECTOR_PAGING)).thenReturn("5");
        assertThat(SelectorUtils.retrieveActivePage(), is(5));
    }

    @Test
    public void testWithNullUrl() {
        assertThat(updateSelectors(null, "pid", "1"), equalTo(""));
    }

    @Test
    public void testWithEmptyUrl() {
        assertThat(updateSelectors("", "pid", "1"), equalTo(""));
    }

    @Test
    public void testWithWhitespaceUrl() {
        assertThat(updateSelectors(" \t ", "pid", "1"), equalTo(""));
    }

    @Test
    public void addSelectorWithNoExtension() {
        assertThat(updateSelectors("/test", "pid", "1"), equalTo("/test~pid=1~.html"));
    }

    @Test
    public void addSelectorWithNoExtensionButQueryString() {
        assertThat(updateSelectors("/test?123", "pid", "1"), equalTo("/test~pid=1~.html?123"));
    }

    @Test
    public void addSelectorWithExtensionAndQueryString() {
        assertThat(updateSelectors("/test.xml?123", "pid", "1"), equalTo("/test~pid=1~.xml?123"));
    }

    @Test
    public void addSelectorWithEncoding() {
        assertThat(updateSelectors("/test.html", "pid", "\u00FC"), equalTo("/test~pid=%C3%BC~.html"));
    }

    @Test
    public void updateSelector() {
        assertThat(updateSelectors("/test~kid=1~pid=2~.html", "pid", "1"), equalTo("/test~kid=1~pid=1~.html"));
    }

    @Test
    public void removeSelector() {
        assertThat(updateSelectors("/test~kid=1~pid=2~.html", "pid", "1", "kid"), equalTo("/test~pid=1~.html"));
    }

    @Before
    public void createMgnlContext() {
        cleanContext();
    }
}
