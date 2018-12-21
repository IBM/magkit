package com.aperto.magkit.utils;

import com.aperto.magkit.mockito.ContextMockUtils;
import info.magnolia.context.MgnlContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
 * @author wolf.bubenik
 * @since 21.12.18.
 */
public class CacheUtilsTest {
    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void preventCaching() throws Exception {
        // test no web context
        CacheUtils.preventCaching();

        ContextMockUtils.mockWebContext();
        CacheUtils.preventCaching();
        verify(MgnlContext.getWebContext().getResponse(), times(1)).setHeader(HEADER_PRAGMA, HEADER_VALUE_NO_CACHE);
        verify(MgnlContext.getWebContext().getResponse(), times(1)).setHeader(HEADER_CACHE_CONTROL, HEADER_CACHE_CONTROL_VALUE_DISABLE_CACHE);
        verify(MgnlContext.getWebContext().getResponse(), times(1)).setDateHeader(HEADER_EXPIRES, 0L);
    }

    @Test
    public void preventCaching1() throws Exception {
        // test no NPE
        CacheUtils.preventCaching(null);
    }

}