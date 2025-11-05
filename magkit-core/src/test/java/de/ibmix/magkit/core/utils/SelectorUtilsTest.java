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

import info.magnolia.cms.core.AggregationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;

import static de.ibmix.magkit.core.utils.SelectorUtils.DEF_PAGE;
import static de.ibmix.magkit.core.utils.SelectorUtils.SELECTOR_PAGING;
import static de.ibmix.magkit.core.utils.SelectorUtils.updateSelectors;
import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubSelector;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAttribute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test of the resource utils.
 *
 * @author frank.sommer (11.12.2008)
 * @see SelectorUtils
 */
public class SelectorUtilsTest {

    @Test
    public void isPagingView() throws RepositoryException {
        AggregationState aggregationState = mockAggregationState();
        assertFalse(SelectorUtils.isPagingView());

        stubSelector("pid").of(aggregationState);
        assertTrue(SelectorUtils.isPagingView());

        stubSelector("pid=3").of(aggregationState);
        assertTrue(SelectorUtils.isPagingView());

        stubSelector("test=pid").of(aggregationState);
        assertFalse(SelectorUtils.isPagingView());
    }

    @Test
    public void isPrintView() throws RepositoryException {
        AggregationState aggregationState = mockAggregationState();
        assertFalse(SelectorUtils.isPrintView());

        stubSelector("print").of(aggregationState);
        assertTrue(SelectorUtils.isPrintView());

        stubSelector("print=true").of(aggregationState);
        assertFalse(SelectorUtils.isPrintView());
    }

    @Test
    public void selectorContains() throws RepositoryException {
        AggregationState aggregationState = mockAggregationState();
        assertFalse(SelectorUtils.selectorContains("test", true));

        stubSelector("key=value~other").of(aggregationState);
        assertFalse(SelectorUtils.selectorContains("test", true));
        assertFalse(SelectorUtils.selectorContains("test", false));

        stubSelector("key=value~other~test=true").of(aggregationState);
        assertTrue(SelectorUtils.selectorContains("test", true));
        assertFalse(SelectorUtils.selectorContains("test", false));

        stubSelector("key=value~other~test").of(aggregationState);
        assertTrue(SelectorUtils.selectorContains("test", true));
        assertTrue(SelectorUtils.selectorContains("test", false));

        stubSelector("key=value~other~issue=test").of(aggregationState);
        assertFalse(SelectorUtils.selectorContains("test", true));
        assertFalse(SelectorUtils.selectorContains("test", false));
    }

    @Test
    public void retrieveActivePageWithNoValue() throws RepositoryException {
        mockWebContext(stubAttribute(SELECTOR_PAGING, null));
        assertEquals(DEF_PAGE, SelectorUtils.retrieveActivePage());
    }

    @Test
    public void retrieveActivePageWithLetterValue() throws RepositoryException {
        mockWebContext(stubAttribute(SELECTOR_PAGING, "abc"));
        assertEquals(DEF_PAGE, SelectorUtils.retrieveActivePage());
    }

    @Test
    public void retrieveActivePageWithInvalidValue() throws RepositoryException {
        mockWebContext(stubAttribute(SELECTOR_PAGING, "-5"));
        assertEquals(DEF_PAGE, SelectorUtils.retrieveActivePage());
    }

    @Test
    public void retrieveActivePageWithValidValue() throws RepositoryException {
        mockWebContext(stubAttribute(SELECTOR_PAGING, "5"));
        assertEquals(5, SelectorUtils.retrieveActivePage());
    }

    @Test
    public void testWithNullUrl() {
        assertEquals("", updateSelectors(null, "pid", "1"));
    }

    @Test
    public void testWithEmptyUrl() {
        assertEquals("", updateSelectors("", "pid", "1"));
    }

    @Test
    public void testWithWhitespaceUrl() {
        assertEquals("", updateSelectors(" \t ", "pid", "1"));
    }

    @Test
    public void addSelectorWithNoExtension() {
        assertEquals("/test~pid=1~.html", updateSelectors("/test", "pid", "1"));
    }

    @Test
    public void addSelectorWithNoExtensionButQueryString() {
        assertEquals("/test~pid=1~.html?123", updateSelectors("/test?123", "pid", "1"));
    }

    @Test
    public void addSelectorWithExtensionAndQueryString() {
        assertEquals("/test~pid=1~.xml?123", updateSelectors("/test.xml?123", "pid", "1"));
    }

    @Test
    public void addSelectorWithEncoding() {
        assertEquals("/test~pid=%C3%BC~.html", updateSelectors("/test.html", "pid", "\u00FC"));
    }

    @Test
    public void updateSelector() {
        assertEquals("/test~kid=1~pid=1~.html", updateSelectors("/test~kid=1~pid=2~.html", "pid", "1"));
    }

    @Test
    public void removeSelector() {
        assertEquals("/test~pid=1~.html", updateSelectors("/test~kid=1~pid=2~.html", "pid", "1", "kid"));
    }

    @BeforeEach
    public void createMgnlContext() {
        cleanContext();
    }
}
