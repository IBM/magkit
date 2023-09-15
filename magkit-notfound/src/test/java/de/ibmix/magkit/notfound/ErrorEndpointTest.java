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

import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the error endpoint.
 *
 * @author frank.sommer
 * @since 04.09.2023
 */
public class ErrorEndpointTest {

    private ErrorEndpoint _endpoint;
    private final NotfoundModule _notfoundModule = new NotfoundModule();
    private AggregationState _aggregationState;

    @Before
    public void setUp() throws Exception {
        SiteManager siteManager = mock(SiteManager.class);
        final ErrorService errorService = new ErrorService(() -> _notfoundModule, () -> siteManager);

        _aggregationState = mock(AggregationState.class);
        _endpoint = new ErrorEndpoint(null, errorService, () -> _aggregationState);

        mockPageNode("/de/error/404");
        mockPageNode("/error/404");
        mockPageNode("/tenant/fr/error/404");

        final Site testSite = mock(Site.class);
        when(siteManager.getAssignedSite(null, "/notfound")).thenReturn(testSite);
        when(siteManager.getAssignedSite(null, "/de/notfound")).thenReturn(testSite);
        when(siteManager.getAssignedSite(null, "/fr/notfound")).thenReturn(testSite);

        final Site tenantSite = mock(Site.class);
        final URI2RepositoryMapping websiteMapping = new URI2RepositoryMapping();
        websiteMapping.setHandlePrefix("/tenant");
        when(tenantSite.getMappings()).thenReturn(Map.of("dam", new URI2RepositoryMapping(), "website", websiteMapping));
        when(siteManager.getAssignedSite("tenant.fr", "/fr/notfound")).thenReturn(tenantSite);
    }

    @Test
    public void topLevelErrorPageExists() {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/notfound");

        assertThat(_endpoint.getErrorPagePath(404), equalTo("/error/404"));
    }

    @Test
    public void defaultErrorPagePathDoesNotExists() {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/fr/notfound");

        assertThat(_endpoint.getErrorPagePath(404), equalTo(""));
    }

    @Test
    public void existingDefaultErrorPagePath() {
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/de/notfound");

        assertThat(_endpoint.getErrorPagePath(404), equalTo("/de/error/404"));
    }

    @Test
    public void existingDefaultErrorPageWithDefaultPath() {
        _notfoundModule.setDefaultErrorPath("/de");
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/notfound");

        assertThat(_endpoint.getErrorPagePath(404), equalTo("/de/error/404"));
    }

    @Test
    public void existingSiteErrorPagePath() {
        _aggregationState = mock(ExtendedAggregationState.class);
        when(_aggregationState.getOriginalBrowserURI()).thenReturn("/fr/notfound");
        when(((ExtendedAggregationState) _aggregationState).getDomainName()).thenReturn("tenant.fr");

        assertThat(_endpoint.getErrorPagePath(404), equalTo("/tenant/fr/error/404"));
    }

    @After
    public void tearDown() {
        cleanContext();
    }
}
