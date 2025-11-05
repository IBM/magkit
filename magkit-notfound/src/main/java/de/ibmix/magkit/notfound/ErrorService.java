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
 * Central service responsible for resolving and providing error page information based on HTTP status codes
 * and Magnolia site configuration. It encapsulates the logic to determine the most specific error page path
 * for a given request context (status code, domain, original URI) by analyzing site mappings, repository handles
 * and configurable module parameters.
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li>Computes an error page path considering site-specific path fragments and configurable relative error path.</li>
 *   <li>Maps status codes to custom error page names via module configuration (fallback to numeric code if unmapped).</li>
 *   <li>Validates existence of candidate error page nodes in the Magnolia JCR <code>website</code> workspace.</li>
 *   <li>Provides a simple entity map representation usable for API responses or templating.</li>
 * </ul>
 *
 * <p>Usage Preconditions:</p>
 * <ul>
 *   <li>Magnolia context must be initialized so that JCR sessions and site resolution work.</li>
 *   <li>Module configuration ({@link NotfoundModule}) must be available through dependency injection.</li>
 * </ul>
 *
 * <p>Null and Error Handling:</p>
 * <ul>
 *   <li>If an error page cannot be resolved or does not exist, an empty string is returned as path.</li>
 *   <li>JCR access issues are logged and suppressed; methods do not propagate repository exceptions.</li>
 * </ul>
 *
 * <p>Thread-Safety: This service is stateless; providers are thread-safe references. All Magnolia context access
 * assumes Magnolia's own thread confinement model for requests.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 *   ErrorService errorService = ...; // injected
 *   String path404 = errorService.retrieveErrorPagePath(404, "www.example.com", "/foo/bar");
 *   Map<String, Object> payload = errorService.createEntity(404, "/foo/bar");
 * }</pre>
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

    /**
     * Creates a simple error entity representation containing the status code and a resolved error page path
     * for the given original node path. The domain is not considered here and defaults to {@code null}.
     * If no specific error page exists an empty string is provided for the page key.
     *
     * @param statusCode the HTTP status code (e.g. 404, 500)
     * @param nodePath the original requested content node path used for site context resolution
     * @return a mutable map with keys: {@code status} (Integer) and {@code page} (String)
     */
    public Map<String, Object> createEntity(int statusCode, String nodePath) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("status", statusCode);
        errorResult.put("page", retrieveErrorPagePath(statusCode, null, nodePath));
        return errorResult;
    }

    /**
     * Determines the best matching error page path for a given HTTP status code within the site context identified
     * by domain and original URI. It composes the candidate path from the site root, optional relative error path
     * and mapped error code name. Existence of the node is validated; otherwise an empty string is returned.
     *
     * @param statusCode the HTTP status code to resolve
     * @param domain the request domain used for site resolution (may be {@code null})
     * @param originalUri the original request URI or node path
     * @return the JCR path of the error page or an empty string if not found
     */
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

    /**
     * Derives the site path (including optional base path fragments) for the given domain and original URI.
     * It uses Magnolia's site manager and repository mapping to determine the site root and then computes
     * additional path fragments based on configured {@code SITE_PARAM_FRAGMENT_LENGTH}.
     *
     * @param domain the domain used for site assignment (may be {@code null})
     * @param originalUri the original request URI or node path
     * @param defaultErrorPath fallback path if no mapping could be resolved
     * @return the resolved site path combined with optional fragment-based base path
     */
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
            if (isNotBlank(basePath) && !basePath.startsWith("/")) {
                basePath = '/' + basePath;
            }
        }
        LOGGER.debug("Base path: {}", basePath);

        return sitePath + basePath;
    }

    /**
     * Checks existence of the candidate error page node path in the Magnolia {@code website} workspace.
     * Logs any repository exception and returns {@code false} on error.
     *
     * @param errorPagePath the candidate error page JCR path
     * @return {@code true} if the node exists; {@code false} otherwise
     */
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

    /**
     * Splits the node path into site-relative fragments constrained by the specified fragment length, ensuring
     * correct removal of the site root prefix. The resulting array length is at most {@code fragmentLength + 1}.
     *
     * @param nodePath the full repository handle
     * @param sitePath the resolved site path (may be empty or root)
     * @param fragmentLength the maximum number of fragment splits (controls granularity)
     * @return an array of site-relative path fragments; possibly empty
     */
    private static String[] getSiteRelativePathFragments(String nodePath, String sitePath, int fragmentLength) {
        String siteRoot = "/";
        if (isNotEmpty(sitePath) && nodePath.startsWith(sitePath)) {
            siteRoot = sitePath;
        }
        return removeStart(nodePath, siteRoot).split("/", fragmentLength + 1);
    }
}
