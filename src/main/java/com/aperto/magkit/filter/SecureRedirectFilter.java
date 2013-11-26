package com.aperto.magkit.filter;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.link.CompleteUrlPathTransformer;
import info.magnolia.link.Link;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.voting.Voter;
import info.magnolia.voting.Voting;
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
    private Voter[] _secure = new Voter[0];
    private LinkTransformerManager _linkTransformer;

    public Voter[] getSecure() {
        return _secure;
    }

    public void addSecure(Voter template) {
        _secure = (Voter[]) ArrayUtils.add(_secure, template);
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
        if (actPage != null) {
            boolean shouldSecure = shouldSecure(request);
            boolean isSecureRequest = isSecureRequest(request);

            LOGGER.debug("Secure: {} and secure template {}.", isSecureRequest, shouldSecure);
            if (isSecureRequest) {
                if (shouldSecure) {
                    chain.doFilter(request, response);
                } else {
                    String mimeType = response.getContentType();
                    if (mimeType.startsWith("text/html")) {
                        tryRedirect(request, response, actPage, false);
                    } else {
                        chain.doFilter(request, response);
                    }
                }
            } else if (shouldSecure) {
                tryRedirect(request, response, actPage, true);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    protected void tryRedirect(HttpServletRequest request, HttpServletResponse response, Content page, boolean secureProtocol) throws IOException {
        CompleteUrlPathTransformer completeUrl = _linkTransformer.getCompleteUrl();
        String link = completeUrl.transform(new Link(page));

        if (secureProtocol) {
            link = changeToSecureUrl(link);
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

    /**
     * Check if the given request is secure.
     */
    protected boolean isSecureRequest(final HttpServletRequest request) {
        return request.isSecure();
    }

    /**
     * Transforms the given link to the secure link.
     */
    protected String changeToSecureUrl(String link) {
        String secureLink = link.replace("http://", "https://");
        if (isNotBlank(_httpPort)) {
            secureLink = secureLink.replace(":" + _httpPort, ":" + _httpsPort);
        }
        return secureLink;
    }

    protected boolean shouldSecure(HttpServletRequest request) {
        Voting voting = Voting.HIGHEST_LEVEL;
        return voting.vote(_secure, request) > 0;
    }
}