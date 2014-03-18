package com.aperto.magkit.filter;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.filters.CosMultipartRequestFilter;
import info.magnolia.cms.filters.MultipartRequestWrapper;
import info.magnolia.context.MgnlContext;
import info.magnolia.voting.Voter;
import info.magnolia.voting.Voting;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * 2. Make maximum file size configurable.<br/>
 * 3. System default usage is configurable, too.
 *
 * @author frank.sommer
 * @see CosMultipartRequestFilter
 * @since 09.07.2008
 */
public class ExtendedMultipartRequestFilter extends CosMultipartRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedMultipartRequestFilter.class);

    /**
     * Max request content size (50 MB).
     */
    public static final int DEFAULT_MAX_SIZE = 52428800;

    /**
     * Maximum allowed request content size.
     */
    private int _maxRequestSize = DEFAULT_MAX_SIZE;
    private Voter[] _useSystemDefault = new Voter[0];

    public void setMaxRequestSize(int maxRequestSize) {
        _maxRequestSize = maxRequestSize;
    }

    public Voter[] getUseSystemDefault() {
        return _useSystemDefault;
    }

    public void addUseSystemDefault(Voter template) {
        _useSystemDefault = (Voter[]) ArrayUtils.add(_useSystemDefault, template);
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (shouldUseSystemDefault(request)) {
            super.doFilter(request, response, chain);
        } else {
            // is almost the same code like in super class
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
            boolean successfulMultipart = false;
            if (isMultipart) {
                try {
                    MultipartForm mpf = parseParameters(request);
                    newRequest = new MultipartRequestWrapper(request, mpf);
                    MgnlContext.push(newRequest, response);
                    successfulMultipart = true;
                } catch (IOException e) {
                    LOGGER.warn("IOException, perhaps to large upload file.", e);
                }
            }
            try {
                chain.doFilter(newRequest, response);
            } finally {
                if (successfulMultipart) {
                    MgnlContext.pop();
                }
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
        MultipartRequest multi = new MultipartRequest(request, Path.getTempDirectoryPath(), _maxRequestSize, encoding, fileRenamePolicy);
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

    protected boolean shouldUseSystemDefault(HttpServletRequest request) {
        Voting voting = Voting.HIGHEST_LEVEL;
        return voting.vote(_useSystemDefault, request) > 0;
    }
}