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
 * Endpoint for rendering HTTP error code handling in Magnolia.
 * <p>
 * Provides two modes of error representation: a default page forward (HTML rendering) and a headless JSON variant.
 * It resolves the target error page path based on status code, domain and original request URI using {@link ErrorService}.
 * This endpoint should be referenced in the error-mapping of the web.xml so that container dispatched error requests
 * reach the appropriate rendering logic.
 * </p>
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Determines error status code from servlet request attributes.</li>
 *   <li>Resolves page handle for site/domain-aware error pages.</li>
 *   <li>Forwards to configured page or returns a 404 response if no mapping exists.</li>
 *   <li>Offers headless JSON representation for API consumers.</li>
 * </ul>
 * <p><strong>Usage Preconditions:</strong> Must be invoked in the context of a Magnolia web request where an
 * {@code ERROR_STATUS_CODE} attribute may have been set by the servlet container.</p>
 * <p><strong>Side Effects:</strong> The {@link #defaultRendering()} method may perform a request forward; in that case
 * the returned {@link Response} is {@code null} and response handling continues in the forwarded resource.</p>
 * <p><strong>Null and Error Handling:</strong> If no error page handle can be resolved for a 404 scenario, a NOT_FOUND
 * {@link Response} with an explanatory message is returned. Status code resolution falls back to 404 when absent.</p>
 * <p><strong>Thread-Safety:</strong> Instances are typically managed by the Magnolia IoC container. No mutable shared
 * state beyond injected dependencies is modified; therefore the class is effectively thread-safe under standard Magnolia
 * request handling assumptions.</p>
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // In web.xml error mapping
 * <error-pag>
 *   <error-code>404</error-code>
 *   <location>/magnoliaError/error/default</location>
 * </error-page>
 * }</pre>
 *
 * @author frank.sommer
 * @since 2023-09-04
 */
@Slf4j
@Path("/error")
public class ErrorEndpoint extends AbstractEndpoint<ConfiguredNodeEndpointDefinition> {
    private final ErrorService _errorService;
    private final Provider<AggregationState> _aggregationStateProvider;

    /**
     * Creates a new error endpoint.
     *
     * @param endpointDefinition Magnolia endpoint definition configuration instance
     * @param errorService service for resolving error page paths and building headless entities
     * @param aggregationStateProvider provider supplying current {@link AggregationState} for domain and original URI
     */
    @Inject
    public ErrorEndpoint(ConfiguredNodeEndpointDefinition endpointDefinition, ErrorService errorService, Provider<AggregationState> aggregationStateProvider) {
        super(endpointDefinition);
        _errorService = errorService;
        _aggregationStateProvider = aggregationStateProvider;
    }

    /**
     * Renders the error by forwarding to the resolved error page path (HTML use case).
     * If no path can be resolved a NOT_FOUND response is returned containing an explanatory message.
     *
     * @return {@code null} when a forward was performed successfully; otherwise a NOT_FOUND {@link Response}
     */
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

    /**
     * Provides a headless JSON representation of the error including resolved page path information.
     *
     * @return a {@link Response} with the appropriate HTTP status code and JSON entity body
     */
    @Path("/headless")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response headlessRendering() {
        final int statusCode = getErrorStatusCode();
        return Response.status(statusCode).entity(_errorService.createEntity(statusCode, getErrorPagePath(statusCode))).build();
    }

    /**
     * Resolves the error page path for a given status code considering domain and original request URI.
     *
     * @param statusCode HTTP error status code to resolve a path for
     * @return the page path/handle or {@code null} if none can be determined
     */
    protected String getErrorPagePath(int statusCode) {
        AggregationState aggregationState = _aggregationStateProvider.get();
        // can not use site from aggregation state, because the current request is on the redirect servlet
        // using original uri and domain from aggregation state
        final String domain = aggregationState instanceof ExtendedAggregationState ? ((ExtendedAggregationState) aggregationState).getDomainName() : null;
        final String originalUri = aggregationState.getOriginalBrowserURI();

        return _errorService.retrieveErrorPagePath(statusCode, domain, originalUri);
    }

    /**
     * Retrieves the error status code from the servlet request (attribute {@code ERROR_STATUS_CODE}).
     * Falls back to {@code 404} if the attribute is absent.
     *
     * @return resolved HTTP status code or 404 if not present
     */
    private static int getErrorStatusCode() {
        return Optional.ofNullable(MgnlContext.getWebContext().getRequest().getAttribute(ERROR_STATUS_CODE))
            .map(obj -> (int) obj)
            .orElse(Response.Status.NOT_FOUND.getStatusCode());
    }
}
