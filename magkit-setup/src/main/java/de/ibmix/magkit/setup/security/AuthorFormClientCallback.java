package de.ibmix.magkit.setup.security;

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

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.security.auth.callback.FormClientCallback;
import info.magnolia.context.WebContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Provides an author-aware Magnolia form client callback that only serves the login form when the current instance is
 * an author/admin server or the requested URI targets an authorized internal path ("/.magnolia"). For all other paths
 * on public (non-admin) servers, the callback deliberately returns HTTP 404 to avoid exposing the login endpoint.
 *
 * Main functionalities and key features:
 * - Delegates normal form handling to {@link FormClientCallback} when authorized.
 * - Suppresses login form exposure for unauthorized public paths by sending a 404 response.
 * - Uses {@link ServerConfiguration} to distinguish author/admin vs public instances.
 *
 * Usage preconditions:
 * - Must be registered/configured as Magnolia authentication client callback.
 * - Dependency injection must provide a valid {@link ServerConfiguration} and required Magnolia infrastructure.
 *
 * Side effects:
 * - May commit the response with a 404 status on unauthorized access attempts.
 *
 * Null and error handling:
 * - Expects non-null {@link HttpServletRequest} and {@link HttpServletResponse} provided by the servlet container.
 * - Logs an error if an {@link IOException} occurs while sending the 404 status; processing then stops.
 *
 * Thread-safety:
 * - Stateless aside from injected, effectively immutable configuration; safe for concurrent servlet container threads.
 *
 * Example:
 * <pre>
 * // Executed within Magnolia context via dependency injection
 * AuthorFormClientCallback callback = ...; // obtained from DI
 * callback.handle(request, response);
 * </pre>
 *
 * @author Jean-Charles Robert, Aperto AG
 * @since 2012-10-01
 */
public class AuthorFormClientCallback extends FormClientCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorFormClientCallback.class);
    private static final String AUTHORIZED_PATH = "/.magnolia";

    @Inject
    private ServerConfiguration _serverConfiguration;

    /**
     * Creates a new callback instance.
     *
     * @param configurationProperties Magnolia configuration properties passed to the superclass.
     * @param webContextProvider provider supplying the current {@link WebContext}.
     */
    public AuthorFormClientCallback(MagnoliaConfigurationProperties configurationProperties, Provider<WebContext> webContextProvider) {
        super(configurationProperties, webContextProvider);
    }

    /**
     * Handles the authentication callback: serves the login form only when the server is admin or when the request URI
     * starts with the authorized internal path (contextPath + "/.magnolia"). Otherwise responds with HTTP 404 to hide
     * the login endpoint on public instances.
     *
     * @param request current HTTP servlet request (must not be null)
     * @param response current HTTP servlet response (must not be null)
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        if (_serverConfiguration.isAdmin() || request.getRequestURI().startsWith(request.getContextPath() + AUTHORIZED_PATH)) {
            // process the login form.
            super.handle(request, response);
        } else {
            // send a 404 Not Found response instead of the login form.
            LOGGER.debug("Unauthorized to display the login form for this path {}.", request.getRequestURI());
            try {
                if (!response.isCommitted()) {
                    response.sendError(SC_NOT_FOUND);
                }
            } catch (IOException e) {
                LOGGER.error("exception while modifying the response", e);
            }
        }
    }
}
