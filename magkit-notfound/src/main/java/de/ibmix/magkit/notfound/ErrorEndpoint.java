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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.service.node.definition.ConfiguredNodeEndpointDefinition;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static info.magnolia.cms.util.RequestDispatchUtil.FORWARD_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.dispatch;
import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
    private final ErrorService _errorService;
    private final Provider<AggregationState> _aggregationStateProvider;

    @Inject
    public ErrorEndpoint(ConfiguredNodeEndpointDefinition endpointDefinition, ErrorService errorService, Provider<AggregationState> aggregationStateProvider) {
        super(endpointDefinition);
        _errorService = errorService;
        _aggregationStateProvider = aggregationStateProvider;
    }

    @Path("/default")
    @GET
    public Response defaultRendering() {
        Response response = null;
        String handle = getErrorPagePath(getErrorStatusCode());
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
        final int statusCode = getErrorStatusCode();
        return Response.status(statusCode).entity(_errorService.createEntity(statusCode, getErrorPagePath(statusCode))).build();
    }

    protected String getErrorPagePath(int statusCode) {
        AggregationState aggregationState = _aggregationStateProvider.get();
        // can not use site from aggregation state, because the current request is on the redirect servlet
        // using original uri and domain from aggregation state
        final String domain = aggregationState instanceof ExtendedAggregationState ? ((ExtendedAggregationState) aggregationState).getDomainName() : null;
        final String originalUri = aggregationState.getOriginalBrowserURI();

        return _errorService.retrieveErrorPagePath(statusCode, domain, originalUri);
    }

    private static int getErrorStatusCode() {
        return Optional.ofNullable(MgnlContext.getWebContext().getRequest().getAttribute(ERROR_STATUS_CODE))
            .map(obj -> (int) obj)
            .orElse(Response.Status.NOT_FOUND.getStatusCode());
    }
}
