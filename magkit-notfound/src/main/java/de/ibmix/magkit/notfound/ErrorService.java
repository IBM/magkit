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
import info.magnolia.context.MgnlContext;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.Components;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.notfound.NotfoundModule.SITE_PARAM_FRAGMENT_LENGTH;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeStart;

/**
 * Central service for error page handling.
 *
 * @author frank.sommer
 * @since 15.09.2023
 */
@Slf4j
public class ErrorService {

    private final Provider<NotfoundModule> _moduleProvider;
    private final Provider<SiteManager> _siteManagerProvider;

    @Inject
    public ErrorService(Provider<NotfoundModule> moduleProvider, Provider<SiteManager> siteManagerProvider) {
        _moduleProvider = moduleProvider;
        _siteManagerProvider = siteManagerProvider;
    }

    public Map<String, Object> createEntity(int statusCode, String nodePath) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("status", statusCode);
        errorResult.put("page", retrieveErrorPagePath(statusCode, null, nodePath));
        return errorResult;
    }

    public String retrieveErrorPagePath(int statusCode, String domain, String originalUri) {
        String handle = "";

        NotfoundModule notfoundModule = _moduleProvider.get();
        String sitePath = determineSitePath(domain, originalUri, notfoundModule.getDefaultErrorPath());
        LOGGER.info("Try to find 404 page for {}.", sitePath);

        String relativeErrorPath = notfoundModule.getRelativeErrorPath();
        if (isNotBlank(relativeErrorPath)) {
            relativeErrorPath = '/' + relativeErrorPath;
        }

        final String status = String.valueOf(statusCode);
        String errorPagePath = sitePath + relativeErrorPath + '/' + notfoundModule.getErrorCodeMapping().getOrDefault(status, status);
        LOGGER.info("Error page path candidate is: {}", errorPagePath);
        if (errorPageExists(errorPagePath)) {
            handle = errorPagePath;
        }

        return handle;
    }

    protected String determineSitePath(final String domain, final String originalUri, String defaultErrorPath) {
        final URI2RepositoryManager uri2RepositoryManager = Components.getComponent(URI2RepositoryManager.class);
        String repositoryHandle = uri2RepositoryManager.getHandle(originalUri);
        String workspace = uri2RepositoryManager.getRepository(originalUri);

        final SiteManager siteManager = _siteManagerProvider.get();
        final Site assignedSite = siteManager.getAssignedSite(domain, repositoryHandle);

        final String sitePath = assignedSite.getMappings().entrySet().stream().filter(entry -> entry.getKey().equals(workspace)).findFirst().map(Map.Entry::getValue).map(URI2RepositoryMapping::getHandlePrefix).orElse(defaultErrorPath);
        LOGGER.debug("Site path: {}", sitePath);
        final int fragmentLength = (int) assignedSite.getParameters().getOrDefault(SITE_PARAM_FRAGMENT_LENGTH, 1);
        final String[] pathFragments = getSiteRelativePathFragments(repositoryHandle, sitePath, fragmentLength);

        String basePath = EMPTY;
        if (pathFragments.length > 0) {
            basePath = StringUtils.join(Arrays.copyOf(pathFragments, pathFragments.length - 1), '/');
            if (isNotBlank(basePath)) {
                basePath = '/' + basePath;
            }
        }
        LOGGER.debug("Base path: {}", basePath);

        return sitePath + basePath;
    }

    private static boolean errorPageExists(String errorPagePath) {
        boolean pageExists = false;
        try {
            Session session = MgnlContext.getJCRSession(WEBSITE);
            pageExists = session.nodeExists(errorPagePath);
        } catch (RepositoryException e) {
            LOGGER.error("Error checking error page exists.", e);
        }
        return pageExists;
    }

    private static String[] getSiteRelativePathFragments(String nodePath, String sitePath, int fragmentLength) {
        String siteRoot = "/";
        if (isNotEmpty(sitePath) && nodePath.startsWith(sitePath)) {
            siteRoot = sitePath;
        }
        return removeStart(nodePath, siteRoot).split("/", fragmentLength + 1);
    }
}
