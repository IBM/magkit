package com.aperto.magkit.servlet;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.Components;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static info.magnolia.cms.util.RequestDispatchUtil.dispatch;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.apache.commons.lang.StringUtils.*;

/**
 * 404 error redirect servlet.
 * Configurate handle of error pages in init parameters:
 * - default
 * - language
 * - tenant_language
 *
 * @author diana.racho (Aperto AG)
 */
public class NotFoundRedirectServlet extends HttpServlet {
    private static final long serialVersionUID = -2569111666576917867L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String originUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
        String part1 = EMPTY;
        String part2 = EMPTY;
        if (isNotBlank(originUri)) {
            int startLevel = 0;
            if (isNotBlank(contextPath) && originUri.startsWith(contextPath)) {
                startLevel++;
            }
            String[] parts = split(originUri, '/');

            if (parts.length > startLevel) {
                part1 = substringBefore(parts[startLevel], ".");
            }
            startLevel++;
            if (parts.length > startLevel) {
                part2 = substringBefore(parts[startLevel], ".");
            }
        }
        String handle = getRedirectHandle(part1, part2);
        response.setStatus(SC_NOT_FOUND);
        if (isNotBlank(handle)) {
            dispatch("forward:" + handle, request, response);
        } else {
            PrintWriter writer = response.getWriter();
            writer.write("404 redirect handles are not configurated.");
        }
    }

    private String getRedirectHandle(String part1, String part2) {
        String handle = getInitParameter(part1 + "_" + part2);
        if (isBlank(handle)) {
            handle = getInitParameter(part1);
            if (isBlank(handle)) {
                I18nContentSupport i18nContentSupport = Components.getComponent(I18nContentSupport.class);
                handle = getInitParameter(i18nContentSupport.getLocale().getLanguage());
                if (isBlank(handle)) {
                    handle = getInitParameter("default");
                }
            }
        }
        return handle;
    }
}