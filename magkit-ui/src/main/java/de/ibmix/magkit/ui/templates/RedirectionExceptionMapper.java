package de.ibmix.magkit.ui.templates;

/*-
 * #%L
 * IBM iX Magnolia Kit UI
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

import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * JAX-RS {@link ExceptionMapper} implementation enabling proper redirect handling for headless Magnolia setups.
 * It converts a {@link RedirectionException} into the underlying {@link Response} object so that HTTP redirect
 * semantics (status code + Location header) are passed through unchanged to the client. This is useful when
 * application code deliberately throws a {@code RedirectionException} to short-circuit further processing while
 * running in an environment where no default mapper is registered.
 *
 * Side effects: None (stateless, does not mutate global state).
 * Null/Error handling: Expects a non-null exception; underlying response is returned as obtained. No additional error handling performed.
 * Thread-safety: Stateless and therefore thread-safe.
 * Usage example:
 *   // Registered via JAX-RS auto-discovery or manual configuration
 *   // Framework invokes mapper when a RedirectionException bubbles up
 *
 * @author frank.sommer
 * @since 2023-09-25
 */
public class RedirectionExceptionMapper implements ExceptionMapper<RedirectionException> {

    /**
     * Returns the original {@link Response} contained in the {@link RedirectionException} so that redirect
     * status and headers propagate to the client.
     *
     * @param exception the redirection exception raised by application logic (must not be null)
     * @return original response including redirect status and headers
     */
    @Override
    public Response toResponse(RedirectionException exception) {
        return exception.getResponse();
    }

}
