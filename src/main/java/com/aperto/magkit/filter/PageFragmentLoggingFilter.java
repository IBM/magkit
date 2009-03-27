package com.aperto.magkit.filter;

import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.currentTimeMillis;
import java.util.Date;
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
public class PageFragmentLoggingFilter extends GenericFilterBean {
    private static final Logger LOGGER = Logger.getLogger(PageFragmentLoggingFilter.class);
    private static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";
    private static final String INCLUDE_DEPTH_ATTRIBUTE = PageFragmentLoggingFilter.class.getName() + ".include_depth";
    private static final String FORWARD_SEPARATOR = "================================================================================";
    private static final String INCLUDE_SEPARATOR = "--------------------------------------------------------------------------------";

    private boolean _enabled = true;
    private String[] _extensions = new String[]{"jsp"};
    private boolean _logIntoResponse = false;

    public void setEnabled(final boolean enabled) {
        _enabled = enabled;
    }

    /**
     * Setup multiple extensions separated by space or comma. Default is jsp only.
     */
    public void setEnabledForExtensions(String extensions) {
        _extensions = split(extensions, ", ");
    }

    public void setLogIntoResponse(final boolean logIntoResponse) {
        _logIntoResponse = logIntoResponse;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (_enabled && LOGGER.isDebugEnabled()) {
            doLoggingFilter(servletRequest, servletResponse, filterChain);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private void doLoggingFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        String servletPath = (String) servletRequest.getAttribute(INCLUDE_SERVLET_PATH_ATTRIBUTE);
        Integer includeDepth = 0;
        if (servletPath == null) {
            servletPath = ((HttpServletRequest) servletRequest).getRequestURI();
        } else {
            includeDepth = (Integer) servletRequest.getAttribute(INCLUDE_DEPTH_ATTRIBUTE);
        }
        if (servletPath != null && isEnabledForExtension(servletPath)) {
            logBefore(servletPath, includeDepth);
            if (_logIntoResponse) {
                insertBefore(servletResponse.getWriter(), servletPath, includeDepth);
            }
        }
        servletRequest.setAttribute(INCLUDE_DEPTH_ATTRIBUTE, includeDepth + 1);
        long startTime = currentTimeMillis();
        try {
            filterChain.doFilter(servletRequest, servletResponse);
            if (_logIntoResponse && servletPath != null && isEnabledForExtension(servletPath)) {
                insertAfter(servletResponse.getWriter(), servletPath, includeDepth, startTime);
            }
        } finally {
            if (servletPath != null && isEnabledForExtension(servletPath)) {
                logAfter(servletPath, includeDepth, startTime);
                if (includeDepth == 0) {
                    servletRequest.removeAttribute(INCLUDE_DEPTH_ATTRIBUTE);
                } else {
                    servletRequest.setAttribute(INCLUDE_DEPTH_ATTRIBUTE, includeDepth);
                }
            }
        }
    }

    protected void logBefore(final String servletPath, final Integer includeDepth) {
        if (includeDepth == 0) {
            LOGGER.debug(FORWARD_SEPARATOR);
            LOGGER.debug("PAGE START: " + servletPath);
        } else {
            String indent = repeat("    ", includeDepth);
            LOGGER.debug(indent + INCLUDE_SEPARATOR);
            LOGGER.debug(indent + "INCLUDE START: " + servletPath);
        }
    }

    protected void logAfter(final String servletPath, final Integer includeDepth, final long startTime) {
        String timeElapsed = ", time " + (currentTimeMillis() - startTime) + " ms";
        if (includeDepth == 0) {
            LOGGER.debug("PAGE END:  " + servletPath + timeElapsed);
            LOGGER.debug(FORWARD_SEPARATOR);
        } else {
            String indent = repeat("    ", includeDepth);
            LOGGER.debug(indent + "INCLUDE END:  " + servletPath + timeElapsed);
            LOGGER.debug(indent + INCLUDE_SEPARATOR);
        }
    }

    protected void insertBefore(final PrintWriter out, final String servletPath, final Integer includeDepth) {
        if (includeDepth > 0) {
            String indent = repeat("    ", includeDepth);
            out.write("\n\n" + indent + "<!-- INCLUDE START: " + servletPath + "-->\n\n");
        }
    }

    protected void insertAfter(final PrintWriter out, final String servletPath, final Integer includeDepth, final long startTime) {
        long endTime = currentTimeMillis();
        String timeElapsed = ", time " + (endTime - startTime) + " ms";
        if (includeDepth == 0) {
            out.write("\n<!-- PAGE END: " + servletPath + timeElapsed + ", " + (new Date(endTime)) + " -->");
        } else {
            String indent = repeat("    ", includeDepth);
            out.write("\n\n" + indent + "<!-- INCLUDE END: " + servletPath + timeElapsed + " -->\n\n");
        }
    }

    private boolean isEnabledForExtension(final String path) {
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