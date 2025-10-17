package de.ibmix.magkit.notfound;

/*-
 * #%L
 * magkit-notfound
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
import lombok.NonNull;

import javax.ws.rs.ClientErrorException;

/**
 * Exception indicating a client-side (4xx) error occurred while rendering a page through the page REST service.
 * <p>
 * Purpose: Represent situations where a requested page cannot be rendered because it was not found (404) or another
 * client error status code applies.
 * </p>
 * <p>
 * Key Features:
 * <ul>
 *   <li>Stores the requested path that triggered the error for diagnostic purposes.</li>
 *   <li>Provides access to the HTTP status code via the {@link ClientErrorException} superclass.</li>
 * </ul>
 * </p>
 * <p>
 * Usage Preconditions: The provided statusCode should be a valid HTTP 4xx client error code (typically 404).
 * </p>
 * <p>
 * Null Handling: The path parameter is annotated with {@link NonNull}; a {@code null} value will result in a validation
 * error before assignment.
 * </p>
 * <p>
 * Side Effects: None. Instances are immutable.
 * </p>
 * <p>
 * Thread-Safety: Immutable state makes this class inherently thread-safe.
 * </p>
 * <p>
 * Example:
 * <pre>
 *   throw new PageRenderingException(404, requestPath);
 * </pre>
 * </p>
 *
 * @author frank.sommer
 * @since 2023-09-14
 */
public class PageRenderingException extends ClientErrorException {

    @Getter
    private final String _path;

    /**
     * Creates a new PageRenderingException capturing the failing page path and HTTP client error status code.
     *
     * @param statusCode the HTTP client error status (expected 4xx, e.g. 404)
     * @param path the requested page path (must not be {@code null})
     */
    public PageRenderingException(final int statusCode, @NonNull final String path) {
        super(statusCode);
        _path = path;
    }

}
