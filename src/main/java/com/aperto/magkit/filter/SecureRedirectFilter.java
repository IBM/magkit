package com.aperto.magkit.filter;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.link.CompleteUrlPathTransformer;
import info.magnolia.link.Link;
import info.magnolia.link.LinkTransformerManager;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static info.magnolia.context.MgnlContext.getAggregationState;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Filter for switching between HTTP and HTTPS.
 * It switches to HTTPS for configured templates.
 * Put this filter
 *
 * @author frank.sommer
 * @since 17.08.2012
 */
public class SecureRedirectFilter extends AbstractMgnlFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecureRedirectFilter.class);

    private String _httpsPort = "";
    private String _httpPort = "";
    private String[] _securedTemplates = new String[0];
    private LinkTransformerManager _linkTransformer;

    public String[] getSecuredTemplates() {
        return _securedTemplates;
    }

    public void addSecuredTemplate(String template) {
        _securedTemplates = (String[]) ArrayUtils.add(_securedTemplates, template);
    }

    public void setHttpsPort(String httpsPort) {
        _httpsPort = httpsPort;
    }

    public void setHttpPort(String httpPort) {
        _httpPort = httpPort;
    }

    @Inject
    public void setLinkTransformer(LinkTransformerManager linkTransformer) {
        _linkTransformer = linkTransformer;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        AggregationState state = getAggregationState();
        Content actPage = state.getMainContent();
        if (actPage != null && _securedTemplates != null) {
            boolean isSecureTemplate = ArrayUtils.contains(_securedTemplates, actPage.getTemplate());
            boolean isSecureRequest = request.isSecure();

            LOGGER.debug("Secure: {} and secure template {}.", isSecureRequest, isSecureTemplate);
            if (isSecureRequest) {
                if (isSecureTemplate) {
                    chain.doFilter(request, response);
                } else {
                    String mimeType = response.getContentType();
                    if (mimeType.startsWith("text/html")) {
                        tryRedirect(request, response, actPage, false);
                    } else {
                        chain.doFilter(request, response);
                    }
                }
            } else if (isSecureTemplate) {
                tryRedirect(request, response, actPage, true);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private void tryRedirect(HttpServletRequest request, HttpServletResponse response, Content page, boolean secureProtocol) throws IOException {
        CompleteUrlPathTransformer completeUrl = _linkTransformer.getCompleteUrl();
        String link = completeUrl.transform(new Link(page));

        if (secureProtocol) {
            link = link.replace("http://", "https://");
            if (isNotBlank(_httpPort)) {
                link = link.replace(":" + _httpPort, ":" + _httpsPort);
            }
        }

        if (link.startsWith(secureProtocol ? "https" : "http")) {
            AggregationState state = getAggregationState();
            String extension = state.getExtension();
            String selector = state.getSelector();

            if (isNotBlank(selector)) {
                link = link.replace("." + extension, selector + "." + extension);
            }
            String queryString = request.getQueryString();
            if (isNotBlank(queryString)) {
                link += "?" + queryString;
            }
            response.sendRedirect(link);
        }
    }
}