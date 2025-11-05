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
 * Utility class providing convenience methods to disable HTTP caching for Magnolia responses by setting
 * appropriate HTTP headers (Pragma, Cache-Control and Expires). These methods ensure that dynamically generated
 * content is not cached by Magnolia nor by upstream proxies or the browser when this is undesirable.
 *
 * <p>Key features:
 * <ul>
 *   <li>Stateless, thread-safe static methods.</li>
 *   <li>Convenience variant operating on the current Magnolia WebContext if available.</li>
 *   <li>Graceful handling of missing WebContext (no action performed).</li>
 *   <li>Null-safe handling of the provided {@link HttpServletResponse}.</li>
 * </ul>
 *
 * <p>Usage preconditions: The no-argument {@link #preventCaching()} method requires a Magnolia WebContext to be active;
 * if none is present, the call is a no-op. The {@link #preventCaching(HttpServletResponse)} method expects a response
 * object that may be null; a null value results in a no-op.
 *
 * <p>Side effects: The response headers "Pragma", "Cache-Control" and "Expires" are overwritten with values that
 * disable caching. Existing header values are replaced.
 *
 * <p>Null and error handling: A null response is ignored silently. No exceptions are thrown by these methods.
 *
 * <p>Thread-safety: The class is thread-safe since it is stateless and only performs idempotent header mutations on the
 * provided response instance.
 *
 * <p>Usage example:
 * <pre>
 *   // In a model, filter or servlet where WebContext is active:
 *   CacheUtils.preventCaching();
 *
 *   // In a custom servlet with direct access to the HttpServletResponse:
 *   CacheUtils.preventCaching(response);
 * </pre>
 *
 * @author frank.sommer
 * @since 2017-07-24
 */
public final class CacheUtils {

    /**
     * Convenience method that applies no-cache HTTP headers to the current Magnolia WebContext response
     * if a WebContext is active; otherwise this method performs no action. Headers set are: Pragma=no-cache,
     * Cache-Control=disable-cache (Magnolia specific constant) and Expires=0 (date header).
     */
    public static void preventCaching() {
        if (MgnlContext.isWebContext()) {
            preventCaching(MgnlContext.getWebContext().getResponse());
        }
    }

    /**
     * Applies no-cache HTTP headers to the provided {@link HttpServletResponse}. If the response is null the method
     * performs no action. Overwrites the following headers: Pragma, Cache-Control and sets Expires date to epoch.
     *
     * @param response the HTTP servlet response to modify; may be null (in which case nothing happens)
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
