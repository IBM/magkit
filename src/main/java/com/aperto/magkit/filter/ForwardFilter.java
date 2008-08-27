package com.aperto.magkit.filter;

import info.magnolia.cms.beans.config.ServerConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Filter for forwarding by forbidden areas.
 *
 * @author frank.sommer (27.08.2008)
 */
public class ForwardFilter implements Filter {
    private String _target;
    private List<String> _exceptionSuffixList;
    private String _responseCode;
    private String _onlyAdminServer;
    private static final Logger LOGGER = Logger.getLogger(ForwardFilter.class);

    /**
     * Implemented init method.
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    /**
     * simply forwards to the given target or gives a 404 if no target is specified.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String path = ((HttpServletRequest) request).getRequestURI();
        boolean onlyAdmin = Boolean.parseBoolean(getOnlyAdminServer());

        if (!hasExceptionSuffix(path) && (onlyAdmin && !ServerConfiguration.getInstance().isAdmin()) || !onlyAdmin) {
            if (StringUtils.isEmpty(getTarget())) {
                ((HttpServletResponse) response).sendError(Integer.parseInt(getResponseCode()));
                LOGGER.debug("Send error.");
            } else {
                RequestDispatcher rd = request.getRequestDispatcher(getTarget());
                rd.forward(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean hasExceptionSuffix(String requestPath) {
        boolean result = false;
        for (String suffix : _exceptionSuffixList) {
            if (requestPath.endsWith(suffix)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Implemented destroy method.
     */
    public void destroy() {
    }

    public String getTarget() {
        return _target;
    }

    public void setTarget(String target) {
        _target = target;
    }

    public String getResponseCode() {
        return StringUtils.isNotEmpty(_responseCode) ? _responseCode : "404";
    }

    public void setResponseCode(String responseCode) {
        _responseCode = responseCode;
    }

    public String getOnlyAdminServer() {
        return StringUtils.isNotEmpty(_onlyAdminServer) ? _onlyAdminServer : "true";
    }

    public void setOnlyAdminServer(String onlyAdminServer) {
        _onlyAdminServer = onlyAdminServer;
    }

    public List getExceptionSuffixList() {
        return _exceptionSuffixList;
    }

    public void setExceptionSuffixList(List exceptionSuffixList) {
        _exceptionSuffixList = exceptionSuffixList;
    }
}
