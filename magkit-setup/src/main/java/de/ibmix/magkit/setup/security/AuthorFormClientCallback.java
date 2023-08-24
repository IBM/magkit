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
 * Sends a login form only if server is author.
 * In case of public server, checks if the path is authorised, otherwise send a http 404 error as response.
 *
 * @author Jean-Charles Robert, Aperto AG
 * @since 01.10.12
 */
public class AuthorFormClientCallback extends FormClientCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorFormClientCallback.class);
    private static final String AUTHORIZED_PATH = "/.magnolia";

    @Inject
    private ServerConfiguration _serverConfiguration;

    public AuthorFormClientCallback(MagnoliaConfigurationProperties configurationProperties, Provider<WebContext> webContextProvider) {
        super(configurationProperties, webContextProvider);
    }

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
