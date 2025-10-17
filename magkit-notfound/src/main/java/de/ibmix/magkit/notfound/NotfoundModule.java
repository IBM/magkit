package de.ibmix.magkit.notfound;

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

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Module class of the Magkit module.
 * <p>
 * Provides configuration settings for handling "not found" (404 and related) error pages in a Magnolia based
 * installation. It encapsulates three main configurable aspects:
 * <ul>
 *   <li><b>defaultErrorPath</b>: An absolute or site-root based default path used when no site specific error page
 *       has been defined.</li>
 *   <li><b>relativeErrorPath</b>: A relative path fragment (defaults to "error") that can be appended to a site base
 *       path to resolve a site specific error page location.</li>
 *   <li><b>errorCodeMapping</b>: A mapping from HTTP status codes (e.g. "404", "500") to page names or path fragments
 *       allowing fine grained selection of error pages per status code.</li>
 * </ul>
 * Additionally this module declares the site parameter key {@link #SITE_PARAM_FRAGMENT_LENGTH} which controls how
 * many base path fragments are considered when resolving site specific error pages.
 * <p>
 * <b>Usage Preconditions:</b> All properties are expected to be initialized by Magnolia module configuration
 * (e.g. via YAML or JCR configuration) before first usage. Defaults ensure non-null values.
 * </p>
 * <p>
 * <b>Null and Error Handling:</b> Fields are initialized to safe defaults (empty string, "error", empty mapping).
 * Setters allow null assignment; callers should avoid passing null to prevent additional null checks downstream.
 * </p>
 * <p>
 * <b>Thread-Safety:</b> This class is <em>not</em> thread-safe. It is typically instantiated and configured once during
 * application/module initialization and then accessed read-only. Concurrent mutations of its properties must be
 * externally synchronized.
 * </p>
 * <p>
 * <b>Side Effects:</b> This class has no side effects; it stores configuration data only.
 * </p>
 * <p>
 * <b>Example Usage:</b>
 * <pre>{@code
 * NotfoundModule cfg = new NotfoundModule();
 * cfg.setDefaultErrorPath("/global/error/index");
 * cfg.setRelativeErrorPath("error");
 * cfg.setErrorCodeMapping(Map.of("404", "not-found", "500", "system-error"));
 * String base = cfg.getDefaultErrorPath();
 * }
 * </code></pre>
 *
 * @author frank.sommer
 * @since 2014-05-12
 */
@Getter
@Setter
public class NotfoundModule {
    public static final String SITE_PARAM_FRAGMENT_LENGTH = "basePathFragmentLength";
    private String _defaultErrorPath = EMPTY;
    private String _relativeErrorPath = "error";
    private Map<String, String> _errorCodeMapping = Map.of();
}
