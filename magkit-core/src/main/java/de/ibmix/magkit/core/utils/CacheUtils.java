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
        if (response != null) {
            response.setHeader(HEADER_PRAGMA, HEADER_VALUE_NO_CACHE);
            response.setHeader(HEADER_CACHE_CONTROL, HEADER_CACHE_CONTROL_VALUE_DISABLE_CACHE);
            response.setDateHeader(HEADER_EXPIRES, 0L);
        }
    }

    private CacheUtils() {
        // private constructor
    }
}
