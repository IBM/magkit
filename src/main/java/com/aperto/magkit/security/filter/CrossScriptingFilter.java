package com.aperto.magkit.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
 * This filter uses a request wrapper which deactivates javascript tags inculed within http parameters or header by encoding them.
 * 
 * @author greatwebguy: http://greatwebguy.com/programming/java/simple-cross-site-scripting-xss-servlet-filter/
 * @author mayo.fragoso
 */
public class CrossScriptingFilter implements Filter {

    private FilterConfig _filterConfig;
    private List<String> _uriFilterList;
    
    /**
     * {@inheritDoc}
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        _filterConfig = filterConfig;
        String initParameter = filterConfig.getInitParameter("uriFilterList");
        if (initParameter != null) {
            _uriFilterList = Arrays.asList(StringUtils.stripAll(StringUtils.split(initParameter, ",")));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {
        _filterConfig = null;
    }

    /**
     * Wraps the request and execute it filtering javascript from http parameters an header.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestUri = ((HttpServletRequest) request).getRequestURI();
        if (_uriFilterList != null && _uriFilterList.contains(requestUri)) {
            chain.doFilter(new CrossScriptingRequestWrapper((HttpServletRequest) request), response);
        } else {
            chain.doFilter(request, response);
        }
    }
}