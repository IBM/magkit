package com.aperto.magkit.utils;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.mockito.WebContextStubbingOperation.stubAttribute;
import static com.aperto.magkit.utils.SelectorUtils.DEF_PAGE;
import static com.aperto.magkit.utils.SelectorUtils.SELECTOR_PAGING;
import static com.aperto.magkit.utils.SelectorUtils.updateSelectors;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * Test of the resource utils.
 *
 * @author frank.sommer (11.12.2008)
 * @see SelectorUtils
 */
public class SelectorUtilsTest {
    @Test
    public void retrieveActivePageWithNoValue() throws RepositoryException {
        mockWebContext(stubAttribute(SELECTOR_PAGING, null));
        assertThat(SelectorUtils.retrieveActivePage(), is(DEF_PAGE));
    }

    @Test
    public void retrieveActivePageWithLetterValue() throws RepositoryException {
        mockWebContext(stubAttribute(SELECTOR_PAGING, "abc"));
        assertThat(SelectorUtils.retrieveActivePage(), is(DEF_PAGE));
    }

    @Test
    public void retrieveActivePageWithInvalidValue() throws RepositoryException {
        mockWebContext(stubAttribute(SELECTOR_PAGING, "-5"));
        assertThat(SelectorUtils.retrieveActivePage(), is(DEF_PAGE));
    }

    @Test
    public void retrieveActivePageWithValidValue() throws RepositoryException {
        mockWebContext(stubAttribute(SELECTOR_PAGING, "5"));
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
