package com.aperto.magkit.controller;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

/**
 * Implements a handler interceptor that allows ip range based access control like it is implemented by aperto debug
 * suite. The default configuration grants access to aperto ip addresses only.
 *
 * @author Norman Wiechmann (Aperto AG)
 * @deprecated TODO: should spring be used anymore?
 */
public class IpRangeAccessControlInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = Logger.getLogger(IpRangeAccessControlInterceptor.class);
    private PathMatcher _pathMatcher = new AntPathMatcher();
    private static final String[] APERTO_REMOTE_HOSTS = new String[]{"127.0.0.1", "localhost", "213.61.132.3", "10.18.*", "*.aperto.de"};
    private String[] _securedUrls = new String[0];
    private String[] _allowedRemoteHosts = new String[0];
    private boolean _allowAccessByAperto = true;

    /**
     * Grants or denies access to the requested url based on the ip address of the remote host.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        boolean accessGranted = true;
        String pathInfo = request.getPathInfo();
        String remoteHost = request.getRemoteHost();
        if (pathInfo != null && remoteHost != null) {
            for (String currentPattern : _securedUrls) {
                if (_pathMatcher.match(currentPattern, pathInfo) && !isAccessGrantedFor(remoteHost)) {
                    accessGranted = false;
                    if (LOGGER.isEnabledFor(Level.WARN)) {
                        LOGGER.warn("Access denied. path:" + pathInfo + ", remoteHost:" + remoteHost);
                    }
                    writeAccessDeniedResponse(response, remoteHost);
                    break;
                }
            }
        }
        return accessGranted;
    }

    private boolean isAccessGrantedFor(final String remoteHost) {
        boolean accessGranted = false;
        if (remoteHost != null) {
            accessGranted = (_allowAccessByAperto && matches(remoteHost, APERTO_REMOTE_HOSTS))
                    || matches(remoteHost, _allowedRemoteHosts);
        }
        return accessGranted;
    }

    private boolean matches(final String remoteHost, final String[] patterns) {
        boolean matches = false;
        for (String pattern : patterns) {
            if (remoteHost.equals(pattern)) {
                matches = true;
                break;
            }
        }
        return matches;
    }

    private void writeAccessDeniedResponse(final HttpServletResponse response, final String remoteHost) {
        try {
            response.setStatus(SC_FORBIDDEN);
            PrintWriter w = response.getWriter();
            w.println("Access denied.");
            w.println("<!-- remote host: " + htmlEscape(remoteHost) + " -->");
            w.flush();
        } catch (IOException e) {
            LOGGER.error("Failed to write access denied response.", e);
        }
    }

    public void setSecuredUrls(final String[] securedUrls) {
        _securedUrls = securedUrls != null ? securedUrls : new String[0];
    }

    public void setAllowedRemoteHosts(final String[] allowedRemoteHosts) {
        _allowedRemoteHosts = allowedRemoteHosts;
    }

    public void setAllowAccessByAperto(final boolean allowAccessByAperto) {
        _allowAccessByAperto = allowAccessByAperto;
    }
}