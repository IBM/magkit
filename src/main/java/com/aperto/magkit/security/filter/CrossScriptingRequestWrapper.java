package com.aperto.magkit.security.filter;

import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This request wrapper deactivates html tags inculed within http parameters or header by escaping them. In the case of cached requests,
 * where the parameters are only included in the query string, every single parameter value is html-escaped and then url-encoded again.
 * @see <a href="http://greatwebguy.com/programming/java/simple-cross-site-scripting-xss-servlet-filter/">greatwebguy</a>
 * @author mayo.fragoso
 */
public class CrossScriptingRequestWrapper extends HttpServletRequestWrapper {

    /**
     * {@inheritDoc}
     */
    public CrossScriptingRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }

    /**
     * {@inheritDoc}
     */
    public String getQueryString() {
        String queryString = urlDecode(super.getQueryString());
        if (queryString != null) {
            String[] tokens = StringUtils.split(queryString, "&=");
            for (int i = 0; i < tokens.length; i++) {
                queryString = queryString.replace(tokens[i], urlEncode(escapeHtml(tokens[i])));
            }
        }
        return queryString;
    }

    private String urlDecode(String string) {
        String result = null;
        if (string != null) {
            try {
                result = URLDecoder.decode(string, getCharacterEncoding());
            } catch (Exception e) {
                result = string;
            }
        }
        return result;
    }

    private String urlEncode(String string) {
        String result = null;
        if (string != null) {
            try {
                result = URLEncoder.encode(string, getCharacterEncoding());
            } catch (Exception e) {
                result = string;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getParameterValues(String name) {
        String[] result = null;
        String[] params = super.getParameterValues(name);
        if (params != null) {
            result = new String[params.length];
            for (int i = 0; i < params.length; i++) {
                result[i] = escapeHtml(params[i]);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String getParameter(String name) {
        return escapeHtml(super.getParameter(name));
    }

    /**
     * {@inheritDoc}
     */
    public String getHeader(String name) {
        return escapeHtml(super.getHeader(name));
    }

    /**
     * Remove javascript and escape html tags.
     */
    private String escapeHtml(String value) {
        String result = value;
        if (value != null) {
            result = result.replaceAll("<[\\p{javaWhitespace}]*script", "");
            result = result.replaceAll("<[\\p{javaWhitespace}]*/script[\\p{javaWhitespace}]*>", "");
            result = result.replaceAll("[\\\"\\\'][\\p{javaWhitespace}]*javascript:(.*)[\\\"\\\']", "\"\"");
            result = result.replaceAll("javascript", "\"javascript\"");
            result = result.replaceAll("eval\\((.*)\\)", "");
            result = StringEscapeUtils.escapeHtml(result);
        }
        return result;
    }
}