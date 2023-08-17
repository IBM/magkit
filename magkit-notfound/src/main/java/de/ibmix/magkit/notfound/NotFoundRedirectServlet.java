package de.ibmix.magkit.notfound;

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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static de.ibmix.magkit.core.utils.LocaleUtil.determineLocaleFromPath;
import static info.magnolia.cms.util.RequestDispatchUtil.FORWARD_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.dispatch;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

/**
 * 404 error redirect servlet.
 * Configured in {@link NotfoundModule}.
 *
 * @author diana.racho (Aperto AG)
 * @author frank.sommer
 */
public class NotFoundRedirectServlet extends HttpServlet {
    private static final long serialVersionUID = -2569111666576917867L;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotFoundRedirectServlet.class);

    private Provider<NotfoundModule> _moduleProvider;
    private Provider<AggregationState> _aggregationStateProvider;
    private Provider<SiteManager> _siteManagerProvider;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String handle = getRedirectHandle();
        response.setStatus(SC_NOT_FOUND);
        if (isNotBlank(handle)) {
            dispatch(FORWARD_PREFIX + handle, request, response);
        } else {
            PrintWriter writer = response.getWriter();
            writer.write("404 redirect handles are not configured.");
        }
    }

    protected String getRedirectHandle() {
        String handle = "";

        NotfoundModule magkitModule = _moduleProvider.get();
        NotFoundConfig notFoundConfig = magkitModule.getNotFoundConfig();

        if (notFoundConfig != null) {
            handle = notFoundConfig.getDefault();
            AggregationState aggregationState = _aggregationStateProvider.get();

            String siteName = determineSiteName(aggregationState);
            String locale = determineLocale(aggregationState);
            LOGGER.info("Try to find 404 mapping for {} - {}.", siteName, locale);

            List<ErrorMapping> errorMappings = notFoundConfig.getErrorMappings();
            for (ErrorMapping errorMapping : errorMappings) {
                if (siteName.equals(errorMapping.getSiteName()) && locale.equals(errorMapping.getLocale())) {
                    handle = errorMapping.getErrorPath();
                    break;
                }
            }
        }

        return handle;
    }

    protected String determineLocale(final AggregationState aggregationState) {
        // can not use i18nContentSupport, because the current request is on the redirect servlet,
        // using original uri from aggregation state
        String locale = determineLocaleFromPath(substringBeforeLast(aggregationState.getOriginalURI(), "."));
        locale = defaultIfBlank(locale, aggregationState.getLocale().toString());
        return locale;
    }

    protected String determineSiteName(final AggregationState aggregationState) {
        String siteName = ErrorMapping.DEF_SITE;
        if (aggregationState instanceof ExtendedAggregationState) {
            // can not use site from aggregation state, because the current request is on the redirect servlet
            // using original uri and domain from aggregation state
            final SiteManager siteManager = _siteManagerProvider.get();
            final ExtendedAggregationState extendedAggregationState = (ExtendedAggregationState) aggregationState;
            final Site assignedSite = siteManager.getAssignedSite(extendedAggregationState.getDomainName(), extendedAggregationState.getOriginalBrowserURI());
            if (assignedSite != null && !"fallback".equals(assignedSite.getName())) {
                siteName = assignedSite.getName();
            }
        }
        return siteName;
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
