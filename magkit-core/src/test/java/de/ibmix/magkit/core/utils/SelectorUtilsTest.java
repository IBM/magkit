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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.jcr.RepositoryException;

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
 * @author frank.sommer (IBM iX)
 * @see SelectorUtils
 * @since 2008-12-11
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

    @ParameterizedTest
    @CsvSource({
        ", true, false",
        ", false, false",
        "key=value~other, true, false",
        "key=value~other, false, false",
        "key=value~other~test=true, true, true",
        "key=value~other~test=false, false, false",
        "key=value~other~test, true, true",
        "key=value~other~test, false, true",
        "key=value~other~issue=test, true, false",
        "key=value~other~issue=test, false, false"
    })
    public void selectorContains(String selector, boolean startsWith, boolean isTrue) throws RepositoryException {
        mockAggregationState(stubSelector(selector));
        if (isTrue) {
            assertTrue(SelectorUtils.selectorContains("test", startsWith));
        } else {
            assertFalse(SelectorUtils.selectorContains("test", startsWith));
        }
    }

    @ParameterizedTest
    @CsvSource({
        ", 1",
        "abc, 1",
        "-5, 1",
        "5, 5"
    })
    public void retrieveActivePage(String attribute, int expected) throws RepositoryException {
        mockWebContext(stubAttribute(SELECTOR_PAGING, attribute));
        assertEquals(expected, SelectorUtils.retrieveActivePage());
    }

    @ParameterizedTest
    @CsvSource({
        ", pid, 1, ''",
        "'', pid, 1, ''",
        " \t , pid, 1, ''",
        "/test, pid, 1, /test~pid=1~.html",
        "/test?123, pid, 1, /test~pid=1~.html?123",
        "/test.xml?123, pid, 1, /test~pid=1~.xml?123",
        "/test.html, pid, \u00FC, /test~pid=%C3%BC~.html",
        "/test~kid=1~pid=2~.html, pid, 1, /test~kid=1~pid=1~.html"
    })
    public void testUpdateSelectors(String uri, String id, String value, String expected) {
        assertEquals(expected, updateSelectors(uri, id, value));
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
