package com.aperto.magkit.utils;

import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletResponse;

import static info.magnolia.cms.cache.CacheConstants.HEADER_CACHE_CONTROL;
import static info.magnolia.cms.cache.CacheConstants.HEADER_CACHE_CONTROL_VALUE_DISABLE_CACHE;
import static info.magnolia.cms.cache.CacheConstants.HEADER_EXPIRES;
import static info.magnolia.cms.cache.CacheConstants.HEADER_PRAGMA;
import static info.magnolia.cms.cache.CacheConstants.HEADER_VALUE_NO_CACHE;

/**
 * Cache util class.
 *
 * @author frank.sommer
 * @since 24.07.2017
 */
public final class CacheUtils {

    /**
     * Sets the no cache headers in response.
     * With this headers Magnolia does not cache such responses.
     */
    public static void preventCaching() {
        if (MgnlContext.isWebContext()) {
            preventCaching(MgnlContext.getWebContext().getResponse());
        }
    }

    /**
     * Sets the no cache headers in response.
     * With this headers Magnolia does not cache such responses.
     */
    public static void preventCaching(HttpServletResponse response) {
        response.setHeader(HEADER_PRAGMA, HEADER_VALUE_NO_CACHE);
        response.setHeader(HEADER_CACHE_CONTROL, HEADER_CACHE_CONTROL_VALUE_DISABLE_CACHE);
        response.setDateHeader(HEADER_EXPIRES, 0L);
    }

    private CacheUtils() {
        // private constructor
    }
}
