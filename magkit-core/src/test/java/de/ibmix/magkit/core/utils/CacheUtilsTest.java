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

import info.magnolia.context.MgnlContext;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletResponse;
import static info.magnolia.cms.cache.CacheConstants.HEADER_CACHE_CONTROL;
import static info.magnolia.cms.cache.CacheConstants.HEADER_CACHE_CONTROL_VALUE_DISABLE_CACHE;
import static info.magnolia.cms.cache.CacheConstants.HEADER_EXPIRES;
import static info.magnolia.cms.cache.CacheConstants.HEADER_PRAGMA;
import static info.magnolia.cms.cache.CacheConstants.HEADER_VALUE_NO_CACHE;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test CacheUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2018-12-21
 */
public class CacheUtilsTest {

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    @Test
    public void preventCaching() throws Exception {
        // test no web context: no NPE
        CacheUtils.preventCaching();

        mockWebContext();
        CacheUtils.preventCaching();
        verify(MgnlContext.getWebContext().getResponse(), times(1)).setHeader(HEADER_PRAGMA, HEADER_VALUE_NO_CACHE);
        verify(MgnlContext.getWebContext().getResponse(), times(1)).setHeader(HEADER_CACHE_CONTROL, HEADER_CACHE_CONTROL_VALUE_DISABLE_CACHE);
        verify(MgnlContext.getWebContext().getResponse(), times(1)).setDateHeader(HEADER_EXPIRES, 0L);
    }

    @Test
    public void preventCaching1() {
        // test no NPE
        CacheUtils.preventCaching(null);

        HttpServletResponse response = mockHttpServletResponse();
        CacheUtils.preventCaching(response);
        verify(response, times(1)).setHeader(HEADER_PRAGMA, HEADER_VALUE_NO_CACHE);
        verify(response, times(1)).setHeader(HEADER_CACHE_CONTROL, HEADER_CACHE_CONTROL_VALUE_DISABLE_CACHE);
        verify(response, times(1)).setDateHeader(HEADER_EXPIRES, 0L);
    }
}
