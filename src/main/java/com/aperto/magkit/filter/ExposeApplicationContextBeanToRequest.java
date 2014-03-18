package com.aperto.magkit.filter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.split;
import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;

/**
 * Exposes one or more beans defined within the spring application context to the current request.
 * The function is compareable with {@link org.springframework.web.servlet.view.InternalResourceView#setExposedContextBeanNames(String[] exposedContextBeanNames)}
 * which comes with spring framework since 2.5.x but can be used without spring enabled request like those used by
 * magnolia cms.
 *
 * @author Norman Wiechmann, Aperto AG
 * @since 2009-03-17
 */
public class ExposeApplicationContextBeanToRequest extends OncePerRequestFilter {
    private static final Logger LOGGER = Logger.getLogger(ExposeApplicationContextBeanToRequest.class);
    private String[] _exposedBeanNames;

    /**
     * Setup multiple bean names separated by space or comma.
     */
    public void setExposedBeanNames(String exposedBeanNames) {
        _exposedBeanNames = split(exposedBeanNames, ", ");
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (_exposedBeanNames != null) {
            exposeBeans(request, getRequiredWebApplicationContext(getServletContext()));
        }
        filterChain.doFilter(request, response);
    }

    private void exposeBeans(HttpServletRequest request, WebApplicationContext webApplicationContext) {
        for (String name : _exposedBeanNames) {
            if (isNotBlank(name) && webApplicationContext.containsBean(name)) {
                Object bean = webApplicationContext.getBean(name);
                request.setAttribute(name, bean);
            } else if (isNotBlank(name) && LOGGER.isEnabledFor(Level.WARN)) {
                LOGGER.warn("No such bean definition. name:" + name);
            }
        }
    }
}