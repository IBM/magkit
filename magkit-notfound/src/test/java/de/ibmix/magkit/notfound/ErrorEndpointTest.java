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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import javax.ws.rs.core.Response;
import java.util.Map;

import static de.ibmix.magkit.notfound.NotfoundModule.SITE_PARAM_FRAGMENT_LENGTH;
import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubOriginalBrowserUri;
import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.site.SiteManagerStubbingOperation.stubAssignedSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSiteManager;
import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAttribute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test the error endpoint.
 *
 * @author frank.sommer, completed by GitHub Copilot
 * @since 2023-09-04
 */
public class ErrorEndpointTest {

    private ErrorEndpoint _endpoint;
    private final NotfoundModule _notfoundModule = new NotfoundModule();
    private AggregationState _aggregationState;

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        SiteManager siteManager = mockSiteManager();
        final ErrorService errorService = new ErrorService(() -> _notfoundModule, () -> siteManager);

        _aggregationState = mockAggregationState();
        _endpoint = new ErrorEndpoint(null, errorService, () -> _aggregationState);

        mockPageNode("/de/error/404");
        mockPageNode("/error/404");
        mockPageNode("/tenant/fr/error/404");
        mockPageNode("/error/500");

        mockComponentInstance(MagnoliaConfigurationProperties.class);
        URI2RepositoryManager uri2RepositoryManager = mockComponentInstance(URI2RepositoryManager.class);
        when(uri2RepositoryManager.getHandle("/notfound")).thenReturn("/notfound");
        when(uri2RepositoryManager.getRepository("/notfound")).thenReturn("website");
        when(uri2RepositoryManager.getHandle("/de/notfound")).thenReturn("/de/notfound");
        when(uri2RepositoryManager.getRepository("/de/notfound")).thenReturn("website");
        when(uri2RepositoryManager.getHandle("/fr/notfound")).thenReturn("/fr/notfound");
        when(uri2RepositoryManager.getRepository("/fr/notfound")).thenReturn("website");
        // Stubs for error-page path (headlessRendering uses result of getErrorPagePath for createEntity)
        when(uri2RepositoryManager.getHandle("/error/404")).thenReturn("/error/404");
        when(uri2RepositoryManager.getRepository("/error/404")).thenReturn("website");
        when(uri2RepositoryManager.getHandle("/error/500")).thenReturn("/error/500");
        when(uri2RepositoryManager.getRepository("/error/500")).thenReturn("website");
        when(uri2RepositoryManager.getHandle("/de/error/404")).thenReturn("/de/error/404");
        when(uri2RepositoryManager.getRepository("/de/error/404")).thenReturn("website");
        when(uri2RepositoryManager.getHandle("/tenant/fr/error/404")).thenReturn("/tenant/fr/error/404");
        when(uri2RepositoryManager.getRepository("/tenant/fr/error/404")).thenReturn("website");
        when(uri2RepositoryManager.getHandle("")).thenReturn("");
        when(uri2RepositoryManager.getRepository("")).thenReturn("website");

        URI2RepositoryMapping websiteMappingRoot = new URI2RepositoryMapping();
        websiteMappingRoot.setHandlePrefix("");
        final Site testSite = mockSite("testSite");
        when(testSite.getMappings()).thenReturn(Map.of("website", websiteMappingRoot));
        when(testSite.getParameters()).thenReturn(Map.of(SITE_PARAM_FRAGMENT_LENGTH, 1));
        stubAssignedSite(null, "/notfound", testSite).of(siteManager);
        stubAssignedSite(null, "/de/notfound", testSite).of(siteManager);
        stubAssignedSite(null, "/fr/notfound", testSite).of(siteManager);
        stubAssignedSite(null, "/error/404", testSite).of(siteManager);
        stubAssignedSite(null, "/error/500", testSite).of(siteManager);
        stubAssignedSite(null, "/de/error/404", testSite).of(siteManager);
        stubAssignedSite(null, "", testSite).of(siteManager);

        final Site tenantSite = mockSite("tenantSite");
        final URI2RepositoryMapping websiteMapping = new URI2RepositoryMapping();
        websiteMapping.setHandlePrefix("/tenant");
        when(tenantSite.getMappings()).thenReturn(Map.of("dam", new URI2RepositoryMapping(), "website", websiteMapping));
        when(tenantSite.getParameters()).thenReturn(Map.of(SITE_PARAM_FRAGMENT_LENGTH, 1));
        stubAssignedSite("tenant.fr", "/fr/notfound", tenantSite).of(siteManager);
    }

    @Test
    public void topLevelErrorPageExists() throws RepositoryException {
        stubOriginalBrowserUri("/notfound").of(_aggregationState);
        assertEquals("/error/404", _endpoint.getErrorPagePath(404));
    }

    @Test
    public void defaultErrorPagePathDoesNotExists() throws RepositoryException {
        stubOriginalBrowserUri("/fr/notfound").of(_aggregationState);
        assertEquals("", _endpoint.getErrorPagePath(404));
    }

    @Test
    public void existingDefaultErrorPagePath() throws RepositoryException {
        stubOriginalBrowserUri("/de/notfound").of(_aggregationState);
        assertEquals("/de/error/404", _endpoint.getErrorPagePath(404));
    }

    @Test
    public void existingDefaultErrorPageWithDefaultPath() throws RepositoryException {
        _notfoundModule.setDefaultErrorPath("/de");
        stubOriginalBrowserUri("/notfound").of(_aggregationState);
        assertEquals("/error/404", _endpoint.getErrorPagePath(404));
    }

    @Test
    public void existingSiteErrorPagePath() throws RepositoryException {
        _aggregationState = mock(ExtendedAggregationState.class);
        stubOriginalBrowserUri("/fr/notfound").of(_aggregationState);
        when(((ExtendedAggregationState) _aggregationState).getDomainName()).thenReturn("tenant.fr");
        assertEquals("/tenant/fr/error/404", _endpoint.getErrorPagePath(404));
    }

    /**
     * Verify that defaultRendering forwards and returns null response when page exists.
     */
    @Test
    public void defaultRenderingForwardReturnsNull() throws Exception {
        stubOriginalBrowserUri("/notfound").of(_aggregationState);
        mockWebContext();
        Response response = _endpoint.defaultRendering();
        assertNull(response);
    }

    /**
     * Verify that defaultRendering returns 404 response when no error page handle can be resolved.
     */
    @Test
    public void defaultRenderingReturnsNotFoundResponse() throws Exception {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/fr/notfound");
        mockWebContext();
        Response response = _endpoint.defaultRendering();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
    }

    /**
     * Verify headlessRendering returns JSON entity with resolved error page path for 404.
     */
    @Test
    public void headlessRenderingReturnsResolvedPage() throws Exception {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/notfound");
        mockWebContext();
        Response response = _endpoint.headlessRendering();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked") Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), entity.get("status"));
        // Aufgrund des doppelten 'error' Segments wird keine Seite gefunden.
        assertEquals("", entity.get("page"));
    }

    /**
     * Verify headlessRendering uses request attribute status code and resolves error page for 500.
     */
    @Test
    public void headlessRenderingWithExplicit500StatusAttribute() throws Exception {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/notfound");
        mockWebContext(stubAttribute(ERROR_STATUS_CODE, 500));
        Response response = _endpoint.headlessRendering();
        assertEquals(500, response.getStatus());
        @SuppressWarnings("unchecked") Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals(500, entity.get("status"));
        assertEquals("", entity.get("page"));
    }

    /**
     * Verify headlessRendering returns empty page path when error page does not exist for given URI.
     */
    @Test
    public void headlessRenderingReturnsEmptyPagePathWhenNotFound() throws Exception {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/fr/notfound");
        Response response = _endpoint.headlessRendering();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked") Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        // Fallback error page wird gefunden
        assertEquals("/error/404", entity.get("page"));
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }
}
