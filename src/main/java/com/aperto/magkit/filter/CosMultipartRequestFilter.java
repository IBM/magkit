package com.aperto.magkit.filter;

import com.aperto.webkit.utils.ExceptionEater;
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Fixed implementation of the multipart request filter.
 *
 * @author frank.sommer (09.07.2008)
 */
public class CosMultipartRequestFilter extends AbstractMgnlFilter {
    private static final Logger LOGGER = Logger.getLogger(CosMultipartRequestFilter.class);

    /**
     * Max file upload size. Default 50 MB.
     */
    private static final int DEFAULT_MAX_FILE_SIZE = 52428800;
    private static int c_configuredMaxFileSize;
    private static final String PROPERTY_MAX_FIEL_SIZE = "upload.maxFileSize";
    static {
        try {
            c_configuredMaxFileSize = Math.abs(NumberUtils.toInt(ResourceBundle.getBundle("environment").getString(PROPERTY_MAX_FIEL_SIZE), DEFAULT_MAX_FILE_SIZE));
        } catch (MissingResourceException e) {
            ExceptionEater.eat(e);
        }
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String type = null;
        String type1 = request.getHeader("Content-Type");
        String type2 = request.getContentType();
        if (type1 == null && type2 != null) {
            type = type2;
        } else if (type2 == null && type1 != null) {
            type = type1;
        } else if (type1 != null) {
            type = (type1.length() > type2.length() ? type1 : type2);
        }
        if ((type != null) && type.toLowerCase().startsWith("multipart/form-data")) {
            MultipartForm mpf = parseParameters(request);
            request = new MultipartRequestWrapper(request, mpf);
        }
        chain.doFilter(request, response);
    }

    /**
     * Adds all request paramaters as request attributes.
     *
     * @param request HttpServletRequest
     */
    private static MultipartForm parseParameters(HttpServletRequest request) throws IOException {
        MultipartForm form = new MultipartForm();
        String encoding = StringUtils.defaultString(request.getCharacterEncoding(), "UTF-8");
        DefaultFileRenamePolicy fileRenamePolicy = new DefaultFileRenamePolicy();
        try {
            MultipartRequest multi = new MultipartRequest(request, Path.getTempDirectoryPath(), c_configuredMaxFileSize, encoding, fileRenamePolicy);
            Enumeration params = multi.getParameterNames();
            while (params.hasMoreElements()) {
                String name = (String) params.nextElement();
                String value = multi.getParameter(name);
                form.addParameter(name, value);
                String[] s = multi.getParameterValues(name);
                if (s != null) {
                    form.addparameterValues(name, s);
                }
            }
            Enumeration files = multi.getFileNames();
            while (files.hasMoreElements()) {
                String name = (String) files.nextElement();
                form.addDocument(name, multi.getFilesystemName(name), multi.getContentType(name), multi.getFile(name));
            }
            request.setAttribute(MultipartForm.REQUEST_ATTRIBUTE_NAME, form);
        } catch(IOException ioe) {
            LOGGER.info("The maximum file size was exceeded.");
        }
        return form;
    }

    static class MultipartRequestWrapper extends HttpServletRequestWrapper {
        private MultipartForm _form;

        /**
         * @param request
         */
        public MultipartRequestWrapper(HttpServletRequest request, MultipartForm form) {
            super(request);
            _form = form;
        }

        /**
         * {@inheritDoc}
         */
        public String getParameter(String name) {
            return _form.getParameter(name);
        }

        /**
         * {@inheritDoc}
         */
        public Map getParameterMap() {
            return _form.getParameters();
        }

        /**
         * {@inheritDoc}
         */
        public Enumeration getParameterNames() {
            return _form.getParameterNames();
        }

        /**
         * {@inheritDoc}
         */
        public String[] getParameterValues(String name) {
            return _form.getParameterValues(name);
        }
    }
}
