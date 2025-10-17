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

import info.magnolia.objectfactory.Components;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * 404 Error mapping for page rendering failures producing a JAX-RS {@link Response}.
 * <p>
 * Purpose: Translates a {@link PageRenderingException} raised during Magnolia page rendering into
 * a JSON (or configured) error entity and appropriate HTTP status code.
 * </p>
 * <p>
 * Main functionalities & key features:
 * <ul>
 *   <li>Acts as a JAX-RS {@link Provider} so it is auto-discovered by the runtime.</li>
 *   <li>Extracts HTTP status and failed node path from the thrown {@link PageRenderingException}.</li>
 *   <li>Delegates creation of the response entity to {@link ErrorService} for consistent error payloads.</li>
 * </ul>
 * </p>
 * <p>
 * Usage preconditions: The Magnolia IoC container must be initialized so that {@link Components}
 * can supply an {@link ErrorService} instance. The exception provided must not be {@code null} and
 * must contain a non-null JAX-RS response object.
 * </p>
 * <p>
 * Side effects: None – this mapper is stateless aside from holding a reference to {@link ErrorService}.
 * </p>
 * <p>
 * Null & error handling: Relies on {@link PageRenderingException} delivering a non-null response and path.
 * If these were null, underlying calls may throw a {@link NullPointerException} (not suppressed here).
 * </p>
 * <p>
 * Thread-safety: The class is effectively thread-safe; it keeps an immutable reference to {@link ErrorService}.
 * </p>
 * <p>
 * Usage example (simplified):
 * <pre>
 * // When a PageRenderingException is thrown inside a JAX-RS resource method,
 * // the runtime invokes this mapper to build a uniform error response.
 * </pre>
 * </p>
 *
 * @author frank.sommer
 * @since 15.09.2023
 */
@Slf4j
@Provider
public class PageRenderingErrorMapping implements ExceptionMapper<PageRenderingException> {

    private final ErrorService _errorService;

    /**
     * Constructs the mapper obtaining the {@link ErrorService} from Magnolia's component provider.
     * Ensures a consistent error entity format for all page rendering failures.
     */
    public PageRenderingErrorMapping() {
        _errorService = Components.getComponent(ErrorService.class);
    }

    /**
     * Maps a {@link PageRenderingException} to a JAX-RS {@link Response} using the exception's status
     * code and failed node path. Delegates payload creation to {@link ErrorService}.
     *
     * @param exception the page rendering exception containing status and path information (must not be null)
     * @return a response carrying an error entity and the original HTTP status code
     */
    @Override
    public Response toResponse(PageRenderingException exception) {
        var statusCode = exception.getResponse().getStatus();
        var failedNodePath = exception.getPath();

        return Response.status(statusCode).entity(_errorService.createEntity(statusCode, failedNodePath)).build();
    }

}
