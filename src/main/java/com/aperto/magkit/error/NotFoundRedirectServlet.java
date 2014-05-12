package com.aperto.magkit.error;

import com.aperto.magkit.module.MagkitModule;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.templatingkit.ExtendedAggregationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static info.magnolia.cms.util.RequestDispatchUtil.dispatch;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * 404 error redirect servlet.
 * Configured in {@link MagkitModule}.
 *
 * @author diana.racho (Aperto AG)
 * @author frank.sommer
 */
public class NotFoundRedirectServlet extends HttpServlet {
    private static final long serialVersionUID = -2569111666576917867L;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotFoundRedirectServlet.class);

    private Provider<MagkitModule> _moduleProvider;
    private Provider<I18nContentSupport> _i18nProvider;
    private Provider<AggregationState> _aggregationStateProvider;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String handle = getRedirectHandle();
        response.setStatus(SC_NOT_FOUND);
        if (isNotBlank(handle)) {
            dispatch("forward:" + handle, request, response);
        } else {
            PrintWriter writer = response.getWriter();
            writer.write("404 redirect handles are not configurated.");
        }
    }

    private String getRedirectHandle() {
        String handle = "";

        MagkitModule magkitModule = _moduleProvider.get();
        NotFoundConfig notFoundConfig = magkitModule.getNotFoundConfig();

        if (notFoundConfig != null) {
            handle = notFoundConfig.getDefault();

            String siteName = ErrorMapping.DEF_SITE;
            AggregationState aggregationState = _aggregationStateProvider.get();
            if (aggregationState instanceof ExtendedAggregationState) {
                siteName = ((ExtendedAggregationState) aggregationState).getSite().getName();
            }
            String locale = _i18nProvider.get().getLocale().toString();
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

    @Inject
    public void setModuleProvider(final Provider<MagkitModule> moduleProvider) {
        _moduleProvider = moduleProvider;
    }

    @Inject
    public void setI18nProvider(final Provider<I18nContentSupport> i18nProvider) {
        _i18nProvider = i18nProvider;
    }

    @Inject
    public void setAggregationStateProvider(final Provider<AggregationState> aggregationStateProvider) {
        _aggregationStateProvider = aggregationStateProvider;
    }
}