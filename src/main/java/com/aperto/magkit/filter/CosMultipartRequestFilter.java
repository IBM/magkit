package com.aperto.magkit.filter;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.filters.MultipartRequestWrapper;
import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.apache.commons.lang.StringUtils.defaultString;

/**
 * Fixed implementation of the multipart request filter.<br/>
 * 1. Adds a filename policy to prevent filename conflicts on upload files.<br/>
 * 2. Make maximum file size configurable.
 *
 * @author frank.sommer
 * @since 09.07.2008
 */
public class CosMultipartRequestFilter extends OncePerRequestAbstractMgnlFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosMultipartRequestFilter.class);

    /**
     * Max file upload size. Default 50 MB.
     */
    private static final int DEFAULT_MAX_FILE_SIZE = 52428800;

    /**
     * Max file upload size on author instance. Default 150 MB.
     */
    private static final int DEFAULT_AUHTOR_MAX_FILE_SIZE = 157286400;

    private String _maxFileSize;

    @Inject
    private ServerConfiguration _serverConfiguration;

    public void setMaxFileSize(String maxFileSize) {
        _maxFileSize = maxFileSize;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String type = null;
        String type1 = request.getHeader("Content-Type");
        String type2 = request.getContentType();
        HttpServletRequest newRequest = request;

        if (type1 == null && type2 != null) {
            type = type2;
        } else if (type2 == null && type1 != null) {
            type = type1;
        } else if (type1 != null) {
            type = (type1.length() > type2.length() ? type1 : type2);
        }

        boolean isMultipart = (type != null) && type.toLowerCase().startsWith("multipart/form-data");
        if (isMultipart) {
            MultipartForm mpf = parseParameters(request);
            newRequest = new MultipartRequestWrapper(request, mpf);
            MgnlContext.push(newRequest, response);
        }
        try {
            chain.doFilter(newRequest, response);
        } finally {
            if (isMultipart) {
                MgnlContext.pop();
            }
        }
    }

    /**
     * Adds all request paramaters as request attributes.
     *
     * @param request HttpServletRequest
     */
    private MultipartForm parseParameters(HttpServletRequest request) throws IOException {
        MultipartForm form = new MultipartForm();
        String encoding = defaultString(request.getCharacterEncoding(), UTF_8);
        DefaultFileRenamePolicy fileRenamePolicy = new DefaultFileRenamePolicy();
        int maxValue = Math.abs(NumberUtils.toInt(_maxFileSize, _serverConfiguration.isAdmin() ? DEFAULT_AUHTOR_MAX_FILE_SIZE : DEFAULT_MAX_FILE_SIZE));
        MultipartRequest multi = new MultipartRequest(request, Path.getTempDirectoryPath(), maxValue, encoding, fileRenamePolicy);
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
        return form;
    }
}