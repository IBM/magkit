package com.aperto.magkit.utils;

import info.magnolia.context.WebContext;
import org.junit.Before;
import org.junit.Test;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.utils.SelectorUtils.DEF_PAGE;
import static com.aperto.magkit.utils.SelectorUtils.SELECTOR_PAGING;
import static org.hamcrest.core.Is.is;
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

    @Before
    public void createMgnlContext() {
        cleanContext();
    }
}
