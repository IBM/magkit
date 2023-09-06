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
import info.magnolia.context.MgnlContext;
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.service.node.definition.ConfiguredNodeEndpointDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Map;

import static de.ibmix.magkit.notfound.NotfoundModule.SITE_PARAM_FRAGMENT_LENGTH;
import static info.magnolia.cms.util.RequestDispatchUtil.FORWARD_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.dispatch;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeStart;

/**
 * Endpoint for rendering http error code handling.
 * This endpoint should be referenced in the error-mapping of the web.xml.
 *
 * @author frank.sommer
 * @since 04.09.2023
 */
@Slf4j
@Path("/error")
public class ErrorEndpoint extends AbstractEndpoint<ConfiguredNodeEndpointDefinition> {
    private Provider<NotfoundModule> _moduleProvider;
    private Provider<AggregationState> _aggregationStateProvider;
    private Provider<SiteManager> _siteManagerProvider;

    public ErrorEndpoint(ConfiguredNodeEndpointDefinition endpointDefinition) {
        super(endpointDefinition);
    }

    @Path("/default")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response defaultRendering() {
        Response response = null;
        String handle = getErrorPagePath();
        if (isNotBlank(handle)) {
            dispatch(FORWARD_PREFIX + handle, MgnlContext.getWebContext().getRequest(), MgnlContext.getWebContext().getResponse());
        } else {
            response = Response.status(Response.Status.NOT_FOUND.getStatusCode(), "404 redirect handles are not configured.").build();
        }
        return response;
    }

    @Path("/headless")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response headlessRendering() {
        return Response.ok().build();
    }

    protected String getErrorPagePath() {
        String handle = "";

        NotfoundModule notfoundModule = _moduleProvider.get();
        AggregationState aggregationState = _aggregationStateProvider.get();
        String sitePath = determineSitePath(aggregationState, notfoundModule.getDefaultErrorPath());
        LOGGER.info("Try to find 404 page for {}.", sitePath);

        String relativeErrorPath = notfoundModule.getRelativeErrorPath();
        if (isNotBlank(relativeErrorPath)) {
            relativeErrorPath = '/' + relativeErrorPath;
        }

        final String status = String.valueOf(MgnlContext.getWebContext().getRequest().getAttribute(ERROR_STATUS_CODE));

        String errorPagePath = sitePath + relativeErrorPath + '/' + notfoundModule.getErrorCodeMapping().getOrDefault(status, status);
        LOGGER.info("Error page path candidate is: {}", errorPagePath);
        if (errorPageExists(errorPagePath)) {
            handle = errorPagePath;
        }

        return handle;
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

    protected String determineSitePath(final AggregationState aggregationState, String defaultErrorPath) {
        // can not use site from aggregation state, because the current request is on the redirect servlet
        // using original uri and domain from aggregation state
        final String domain = aggregationState instanceof ExtendedAggregationState ? ((ExtendedAggregationState) aggregationState).getDomainName() : null;
        final String originalUri = aggregationState.getOriginalBrowserURI();

        final SiteManager siteManager = _siteManagerProvider.get();
        final Site assignedSite = siteManager.getAssignedSite(domain, originalUri);

        final String sitePath = assignedSite.getMappings().entrySet().stream().filter(entry -> entry.getKey().equals(WEBSITE)).findFirst().map(Map.Entry::getValue).map(URI2RepositoryMapping::getHandlePrefix).orElse(defaultErrorPath);
        LOGGER.debug("Site path: {}", sitePath);
        final int fragmentLength = (int) assignedSite.getParameters().getOrDefault(SITE_PARAM_FRAGMENT_LENGTH, 1);
        final String[] pathFragments = getSiteRelativePathFragments(originalUri, sitePath, fragmentLength);

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

    private static String[] getSiteRelativePathFragments(String originalUri, String sitePath, int fragmentLength) {
        String siteRoot = "/";
        if (isNotEmpty(sitePath) && originalUri.startsWith(sitePath)) {
            siteRoot = sitePath;
        }
        return removeStart(originalUri, siteRoot).split("/", fragmentLength + 1);
    }

    @Inject
    public void setModuleProvider(final Provider<NotfoundModule> moduleProvider) {
        _moduleProvider = moduleProvider;
    }

    @Inject
    public void setAggregationStateProvider(final Provider<AggregationState> aggregationStateProvider) {
        _aggregationStateProvider = aggregationStateProvider;
    }

    @Inject
    public void setSiteManagerProvider(final Provider<SiteManager> siteManagerProvider) {
        _siteManagerProvider = siteManagerProvider;
    }
}
