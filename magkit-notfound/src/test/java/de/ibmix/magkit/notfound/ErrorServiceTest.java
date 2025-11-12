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

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.ibmix.magkit.notfound.NotfoundModule.SITE_PARAM_FRAGMENT_LENGTH;
import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.site.SiteManagerStubbingOperation.stubAssignedSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSiteManager;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Additional unit tests for {@link ErrorService} to cover configuration edge cases:
 * <ul>
 *   <li>Blank relative error path (no extra segment).</li>
 *   <li>Error code mapping overrides numeric status.</li>
 *   <li>Fallback to defaultErrorPath if no site mapping matches workspace.</li>
 *   <li>Base path construction with fragment length > 1.</li>
 *   <li>Non existing error page path returns empty string.</li>
 * </ul>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-21
 */
public class ErrorServiceTest {

    private NotfoundModule _module;
    private SiteManager _siteManager;
    private ErrorService _service;

    @BeforeEach
    public void setUp() throws Exception {
        cleanContext();
        mockComponentInstance(MagnoliaConfigurationProperties.class);
        mockWebContext(stubJcrSession(WEBSITE));
        _module = new NotfoundModule();
        _siteManager = mockSiteManager();
        _service = new ErrorService(() -> _module, () -> _siteManager);
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    @Test
    public void blankRelativeErrorPathUsesDirectStatusSegment() throws Exception {
        _module.setRelativeErrorPath("");
        mockUriToRepositoryManager("/de/notfound", "website");
        initSiteManagerWithSite("/de", "/de/notfound", 1);
        mockPageNode("/de/404");
        assertEquals("/de/404", _service.retrieveErrorPagePath(404, null, "/de/notfound"));
    }

    @Test
    public void errorCodeMappingOverridesStatus() throws Exception {
        _module.setErrorCodeMapping(Map.of("404", "not-found"));
        mockUriToRepositoryManager("/de/notfound", "website");
        initSiteManagerWithSite("/de", "/de/notfound", 1);
        mockPageNode("/de/error/not-found");
        assertEquals("/de/error/not-found", _service.retrieveErrorPagePath(404, null, "/de/notfound"));
    }

    @Test
    public void fallbackToDefaultErrorPathWhenNoMappingMatches() throws Exception {
        _module.setDefaultErrorPath("/global");
        URI2RepositoryManager uri2RepositoryManager = mockComponentInstance(URI2RepositoryManager.class);
        when(uri2RepositoryManager.getHandle("/unknown/notfound")).thenReturn("/unknown/notfound");
        when(uri2RepositoryManager.getRepository("/unknown/notfound")).thenReturn("website");
        Site site = mock(Site.class);
        when(site.getMappings()).thenReturn(Map.of());
        when(site.getParameters()).thenReturn(Map.of(SITE_PARAM_FRAGMENT_LENGTH, 1));
        when(_siteManager.getAssignedSite(null, "/unknown/notfound")).thenReturn(site);
        mockPageNode("/global/unknown/error/404");
        assertEquals("/global/unknown/error/404", _service.retrieveErrorPagePath(404, null, "/unknown/notfound"));
    }

    @Test
    public void basePathConstructionWithFragmentLengthGreaterThanOne() throws Exception {
        _module.setDefaultErrorPath("");
        mockUriToRepositoryManager("/de/section/sub/notfound", "website");
        initSiteManagerWithSite("/de", "/de/section/sub/notfound", 2);
        mockPageNode("/de/section/error/404");
        assertEquals("/de/section/error/404", _service.retrieveErrorPagePath(404, null, "/de/section/sub/notfound"));
    }

    @Test
    public void errorPageNotExistingReturnsEmptyPath() {
        mockUriToRepositoryManager("/de/notfound", "website");
        initSiteManagerWithSite("/de", "/de/notfound", 1);
        assertEquals("", _service.retrieveErrorPagePath(500, null, "/de/notfound"));
    }

    @Test
    public void customRelativeErrorPathUsed() throws Exception {
        _module.setRelativeErrorPath("errors");
        mockUriToRepositoryManager("/de/notfound", "website");
        initSiteManagerWithSite("/de", "/de/notfound", 1);
        mockPageNode("/de/errors/404");
        assertEquals("/de/errors/404", _service.retrieveErrorPagePath(404, null, "/de/notfound"));
    }

    private void mockUriToRepositoryManager(String handle, String repository) {
        URI2RepositoryManager uri2RepositoryManager = mockComponentInstance(URI2RepositoryManager.class);
        when(uri2RepositoryManager.getHandle(handle)).thenReturn(handle);
        when(uri2RepositoryManager.getRepository(handle)).thenReturn(repository);
    }

    private void initSiteManagerWithSite(String handlePrefix, String requestUri, int basePathFagmentLength) {
        Site site = mock(Site.class);
        URI2RepositoryMapping mapping = new URI2RepositoryMapping();
        if (handlePrefix != null) {
            mapping.setHandlePrefix(handlePrefix);
        }
        when(site.getMappings()).thenReturn(Map.of("website", mapping));
        when(site.getParameters()).thenReturn(Map.of(SITE_PARAM_FRAGMENT_LENGTH, basePathFagmentLength));
        stubAssignedSite(null, requestUri, site).of(_siteManager);
    }
}
