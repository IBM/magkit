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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.context.WebContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.cms.beans.config.ServerConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.inject.Provider;
import java.io.IOException;
import java.lang.reflect.Field;

import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubIsAdmin;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubRequestUri;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AuthorFormClientCallback} covering all conditional branches.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-26
 */
public class AuthorFormClientCallbackTest {

    /**
     * Creates a callback and injects the given server configuration mock.
     *
     * @param serverConfiguration mocked server configuration
     * @return configured callback instance
     */
    private AuthorFormClientCallback createCallback(ServerConfiguration serverConfiguration) throws RepositoryException {
        MagnoliaConfigurationProperties cfg = Mockito.mock(MagnoliaConfigurationProperties.class);
        WebContext ctx = ContextMockUtils.mockWebContext();
        Provider<WebContext> webCtxProvider = () -> ctx;
        AuthorFormClientCallback callback = new AuthorFormClientCallback(cfg, webCtxProvider);
        injectServerConfiguration(callback, serverConfiguration);
        return callback;
    }

    /**
     * Injects the server configuration via reflection since field is package private.
     *
     * @param callback instance under test
     * @param serverConfiguration mock to inject
     */
    private void injectServerConfiguration(AuthorFormClientCallback callback, ServerConfiguration serverConfiguration) {
        try {
            Field f = AuthorFormClientCallback.class.getDeclaredField("_serverConfiguration");
            f.setAccessible(true);
            f.set(callback, serverConfiguration);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to inject ServerConfiguration", e);
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    /**
     * Verifies that authorized internal path (/.magnolia) on non-admin servers does not send 404.
     *
     * @throws IOException never thrown
     */
    @ParameterizedTest
    @CsvSource({
        "true, /ctx, /ctx/somePath, false",
        "false, /ctx, /ctx/.magnolia/login, false",
        "false, /ctx, /ctx/other, true",
        "false, '', /.magnolia/login, false",

    })
    public void handleNo404(boolean isAdmin, final String contextPath, final String requestUri, boolean isCommitedResponse) throws IOException, RepositoryException {
        ServerConfiguration serverConfiguration = mockServerConfiguration(stubIsAdmin(isAdmin));
        AuthorFormClientCallback callback = createCallback(serverConfiguration);
        // avoid processing the login form, disable the callback
        callback.setEnabled(false);
        HttpServletRequest request = mockHttpServletRequest(stubContextPath(contextPath), stubRequestUri(requestUri));
        HttpServletResponse response = mockHttpServletResponse();
        doReturn(isCommitedResponse).when(response).isCommitted();
        callback.handle(request, response);
        verify(response, never()).sendError(Mockito.anyInt());
    }

    /**
     * Verifies that unauthorized path triggers a 404 when response not committed.
     *
     * @throws IOException propagated from mocked sendError
     */
    @Test
    public void handleUnauthorizedPathSends404() throws IOException, RepositoryException {
        ServerConfiguration serverConfiguration = mockServerConfiguration(stubIsAdmin(false));
        AuthorFormClientCallback callback = createCallback(serverConfiguration);
        HttpServletRequest request = mockHttpServletRequest(stubContextPath("/ctx"), stubRequestUri("/ctx/public/page"));
        HttpServletResponse response = mockHttpServletResponse();
        callback.handle(request, response);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Verifies that IOException from sendError is caught without propagating.
     *
     * @throws IOException thrown by mocked sendError
     */
    @Test
    public void handleUnauthorizedPathIOExceptionCaught() throws IOException, RepositoryException {
        ServerConfiguration serverConfiguration = mockServerConfiguration(stubIsAdmin(false));
        AuthorFormClientCallback callback = createCallback(serverConfiguration);
        HttpServletRequest request = mockHttpServletRequest(stubContextPath("/ctx"), stubRequestUri("/ctx/some"));
        HttpServletResponse response = mockHttpServletResponse();
        doReturn(false).when(response).isCommitted();
        doThrow(new IOException("test")).when(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        assertDoesNotThrow(() -> callback.handle(request, response));
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
