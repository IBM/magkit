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
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAttribute;

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

    @Before
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
        assertThat(_endpoint.getErrorPagePath(404), equalTo("/error/404"));
    }

    @Test
    public void defaultErrorPagePathDoesNotExists() throws RepositoryException {
        stubOriginalBrowserUri("/fr/notfound").of(_aggregationState);
        assertThat(_endpoint.getErrorPagePath(404), equalTo(""));
    }

    @Test
    public void existingDefaultErrorPagePath() throws RepositoryException {
        stubOriginalBrowserUri("/de/notfound").of(_aggregationState);
        assertThat(_endpoint.getErrorPagePath(404), equalTo("/de/error/404"));
    }

    @Test
    public void existingDefaultErrorPageWithDefaultPath() throws RepositoryException {
        _notfoundModule.setDefaultErrorPath("/de");
        stubOriginalBrowserUri("/notfound").of(_aggregationState);
        assertThat(_endpoint.getErrorPagePath(404), equalTo("/error/404"));
    }

    @Test
    public void existingSiteErrorPagePath() throws RepositoryException {
        _aggregationState = mock(ExtendedAggregationState.class);
        stubOriginalBrowserUri("/fr/notfound").of(_aggregationState);
        when(((ExtendedAggregationState) _aggregationState).getDomainName()).thenReturn("tenant.fr");
        assertThat(_endpoint.getErrorPagePath(404), equalTo("/tenant/fr/error/404"));
    }

    /**
     * Verify that defaultRendering forwards and returns null response when page exists.
     */
    @Test
    public void defaultRenderingForwardReturnsNull() throws Exception {
        stubOriginalBrowserUri("/notfound").of(_aggregationState);
        mockWebContext();
        Response response = _endpoint.defaultRendering();
        assertThat(response, equalTo(null));
    }

    /**
     * Verify that defaultRendering returns 404 response when no error page handle can be resolved.
     */
    @Test
    public void defaultRenderingReturnsNotFoundResponse() throws Exception {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/fr/notfound");
        mockWebContext();
        Response response = _endpoint.defaultRendering();
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        assertThat(response.getEntity(), equalTo(null));
    }

    /**
     * Verify headlessRendering returns JSON entity with resolved error page path for 404.
     */
    @Test
    public void headlessRenderingReturnsResolvedPage() throws Exception {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/notfound");
        mockWebContext();
        Response response = _endpoint.headlessRendering();
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        @SuppressWarnings("unchecked") Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertThat(entity.get("status"), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        // Aufgrund des doppelten 'error' Segments wird keine Seite gefunden.
        assertThat(entity.get("page"), equalTo(""));
    }

    /**
     * Verify headlessRendering uses request attribute status code and resolves error page for 500.
     */
    @Test
    public void headlessRenderingWithExplicit500StatusAttribute() throws Exception {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/notfound");
        mockWebContext(stubAttribute(ERROR_STATUS_CODE, 500));
        Response response = _endpoint.headlessRendering();
        assertThat(response.getStatus(), equalTo(500));
        @SuppressWarnings("unchecked") Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertThat(entity.get("status"), equalTo(500));
        assertThat(entity.get("page"), equalTo(""));
    }

    /**
     * Verify headlessRendering returns empty page path when error page does not exist for given URI.
     */
    @Test
    public void headlessRenderingReturnsEmptyPagePathWhenNotFound() throws Exception {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/fr/notfound");
        Response response = _endpoint.headlessRendering();
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        @SuppressWarnings("unchecked") Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        // Fallback error page wird gefunden
        assertThat(entity.get("page"), equalTo("/error/404"));
    }

    @After
    public void tearDown() {
        cleanContext();
    }
}
