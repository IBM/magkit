package com.aperto.magkit.filter;

import java.io.IOException;
import static java.lang.System.currentTimeMillis;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import static org.apache.commons.lang.StringUtils.repeat;
import static org.apache.commons.lang.StringUtils.split;
import org.apache.log4j.Logger;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filter that logs start and end of forward and include requests.
 * It replaces the begin.jspf / end.jspf jsp include directives.
 * <p/>
 * Add following code to your web.xml:
 * {@code <filter>
 * <filter-name>forwardAndIncludeLoggingFilter</filter-name>
 * <filter-class>com.aperto.magkit.filter.ForwardAndIncludeLoggingFilter</filter-class>
 * </filter>
 * <filter-mapping>
 * <filter-name>forwardAndIncludeLoggingFilter</filter-name>
 * <url-pattern>/templates/*</url-pattern>
 * <dispatcher>FORWARD</dispatcher>
 * <dispatcher>INCLUDE</dispatcher>
 * </filter-mapping>}
 *
 * @author Norman Wiechmann, Aperto AG
 * @since 2009-03-26
 */
public class ForwardAndIncludeLoggingFilter extends GenericFilterBean {
    private static final Logger LOGGER = Logger.getLogger(ForwardAndIncludeLoggingFilter.class);
    private static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";
    private static final String INCLUDE_DEPTH_ATTRIBUTE = ForwardAndIncludeLoggingFilter.class.getName() + ".include_depth";
    private static final String FORWARD_SEPARATOR = "================================================================================";
    private static final String INCLUDE_SEPARATOR = "--------------------------------------------------------------------------------";

    private String[] _extensions = new String[]{"jsp"};

    /**
     * Setup multiple extensions separated by space or comma. Default is jsp only.
     */
    public void setEnabledForExtensions(String extensions) {
        _extensions = split(extensions, ", ");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String includePath = null;
        String requestPath = null;
        Integer includeDepth = null;
        String indent = null;
        long startTime = 0;
        if (LOGGER.isDebugEnabled()) {
            includePath = (String) servletRequest.getAttribute(INCLUDE_SERVLET_PATH_ATTRIBUTE);
            requestPath = ((HttpServletRequest) servletRequest).getRequestURI();
            if (includePath == null) {
                if (requestPath != null && isEnabledForExtension(requestPath)) {
                    LOGGER.debug(FORWARD_SEPARATOR);
                    LOGGER.debug("PAGE START: " + requestPath);
                    servletRequest.setAttribute(INCLUDE_DEPTH_ATTRIBUTE, 1);
                }
            } else if (isEnabledForExtension(includePath)) {
                includeDepth = (Integer) servletRequest.getAttribute(INCLUDE_DEPTH_ATTRIBUTE);
                servletRequest.setAttribute(INCLUDE_DEPTH_ATTRIBUTE, includeDepth + 1);
                indent = repeat("    ", includeDepth);
                LOGGER.debug(indent + INCLUDE_SEPARATOR);
                LOGGER.debug(indent + "INCLUDE START: " + includePath);
            }
            startTime = currentTimeMillis();
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            if (LOGGER.isDebugEnabled()) {
                String timeElapsed = ", time " + (currentTimeMillis() - startTime) + " ms";
                if (includePath == null) {
                    if (requestPath != null && isEnabledForExtension(requestPath)) {
                        LOGGER.debug("PAGE END:  " + requestPath + timeElapsed);
                        LOGGER.debug(FORWARD_SEPARATOR);
                        servletRequest.removeAttribute(INCLUDE_DEPTH_ATTRIBUTE);
                    }
                } else if (isEnabledForExtension(includePath)) {
                    LOGGER.debug(indent + "INCLUDE END:  " + includePath + timeElapsed);
                    LOGGER.debug(indent + INCLUDE_SEPARATOR);
                    servletRequest.setAttribute(INCLUDE_DEPTH_ATTRIBUTE, includeDepth);
                }
            }
        }
    }

    protected boolean isEnabledForExtension(final String path) {
        boolean isEnabled = false;
        for (String extension : _extensions) {
            if (path.endsWith("." + extension)) {
                isEnabled = true;
                break;
            }
        }
        return isEnabled;
    }
}