package com.aperto.magkit.auth.callbak;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.security.auth.callback.FormClientCallback;
import info.magnolia.init.MagnoliaConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    public AuthorFormClientCallback(MagnoliaConfigurationProperties configurationProperties) {
        super(configurationProperties);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) {

        if (ServerConfiguration.getInstance().isAdmin() || request.getRequestURI().startsWith(request.getContextPath() + AUTHORIZED_PATH)) {
            // process the login form.
            super.handle(request, response);
        } else {
            // send a 404 Not Found response instead of the login form.
            LOGGER.debug("Unauthorized to display the login form for this path {0}.", request.getRequestURI());
            try {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (IOException e) {
                LOGGER.error("exception while modifying the response", e);
            }

        }
    }
}
