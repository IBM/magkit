package com.aperto.magkit.security.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * This filter uses a request wrapper which deactivates javascript tags inculed within http parameters or header by encoding them.
 * 
 * @author greatwebguy: http://greatwebguy.com/programming/java/simple-cross-site-scripting-xss-servlet-filter/
 * @author mayo.fragoso
 */
public class CrossScriptingFilter implements Filter {

    private FilterConfig _filterConfig;

    /**
     * {@inheritDoc}
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        _filterConfig = filterConfig;
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
        chain.doFilter(new CrossScriptingRequestWrapper((HttpServletRequest) request), response);
    }
}