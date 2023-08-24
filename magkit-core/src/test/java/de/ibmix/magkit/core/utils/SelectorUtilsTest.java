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

import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;

import static de.ibmix.magkit.core.utils.SelectorUtils.DEF_PAGE;
import static de.ibmix.magkit.core.utils.SelectorUtils.SELECTOR_PAGING;
import static de.ibmix.magkit.core.utils.SelectorUtils.updateSelectors;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAttribute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

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
