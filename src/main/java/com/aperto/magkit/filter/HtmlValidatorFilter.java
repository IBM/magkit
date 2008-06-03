package com.aperto.magkit.filter;

import com.aperto.webkit.lang.IoRuntimeException;
import com.aperto.webkit.utils.IoTools;
import com.aperto.webkit.utils.StringTools;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * Complete class from aperto commons needed. Because elimination of Magnolia MainBar needed to validate HTML
 * and generating HTML is private inside this class.
 *
 * @author Michael Tamm, frank.sommer
 */
public class HtmlValidatorFilter extends AbstractMgnlFilter {
    private static final Logger LOGGER = Logger.getLogger(HtmlValidatorFilter.class);
    
    public static final String W3C_VALIDATOR_CHECK_URL_PARAM_NAME = "w3c-validator-check-url";
    public static final String VALIDATOR_WARNING_CSS_URI = "validator-warning-css-uri";
    private static final String VALIDATION_RESULT_URL_PREFIX = "/html-validator-result-";
    private static final String VALIDATION_RESULT_URL_SUFFIX = ".html";
    public static final String MAGNOLIA_MAIN_BAR_START_PARAM = "magnolia-main-bar-start";
    public static final String MAGNOLIA_MAIN_BAR_END_PARAM = "magnolia-main-bar-end";
    private static final int MAX_CACHED_RESULTS = 100;
    private static final String PROPERTY_VALIDATOR_ACTIVE = "validator.active";
    private static final String MGNL_MAIN_BAR_BEGIN = "<div class=\"mgnlMainbar";
    private static final String MGNL_MAIN_BAR_END = "</div>";
    
    // using old behaviour (no such filter) as default:
    private static boolean c_validatorEnabled = false;
    static {
        try {
            c_validatorEnabled = BooleanUtils.toBoolean(ResourceBundle.getBundle("environment").getString(PROPERTY_VALIDATOR_ACTIVE));
        } catch (MissingResourceException e) {
            // ignore, validation is disabled (default seting)
        }
    }

    private String _w3cValidatorCheckUrl = "http://validator.aperto.de/w3c-markup-validator/check";
    private String _validatorWarningCssUri = "/css/validator-warning.css";
    private String _warningLayerTemplate;
    private int _resultCounter = 0;
    private String[] _cachedResults = new String[MAX_CACHED_RESULTS];

    /**
     * inits the filter.
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("init(" + filterConfig + ")");
        }
        String templateLocation = getClass().getName();
        templateLocation = templateLocation.replace('.', '/') + "$WarningLayerTemplate.html";
        InputStream in = getClass().getClassLoader().getResourceAsStream(templateLocation);
        try {
            if (in == null) {
                throw new RuntimeException("Could not load " + templateLocation + ".");
            } else {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                IoTools.copy(in, buffer);
                _warningLayerTemplate = buffer.toString();
            }
        } finally {
            IoTools.closeQuietly(in);
        }
    }

    /**
     * destoys the filter.
     */
    public void destroy() {
        LOGGER.debug("destroy()");
    }

    /**
     * Only filters for valid html if not magnolia admin pages (starting with a .) or aperto debug suite. 
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (c_validatorEnabled) {
            String requestUri = request.getRequestURI();
            String context = request.getContextPath();
            if (!(requestUri.startsWith(context + "/.") || requestUri.startsWith(context + "/debug"))) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("doFilter(" + request + ", " + response + ", " + chain + ")");
                }
                filter(request, response, chain);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
    private void filter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String url = request.getRequestURL().toString();
        int i = url.indexOf(VALIDATION_RESULT_URL_PREFIX);
        if (i != -1) {
            // Extract validation result number ...
            String s = url.substring(i + VALIDATION_RESULT_URL_PREFIX.length());
            s = s.substring(0, s.length() - VALIDATION_RESULT_URL_SUFFIX.length());
            i = StringTools.asInt(s);
            // Display w3c validator result ...
            String html;
            if (i < (_resultCounter - MAX_CACHED_RESULTS)) {
                html = "<span style=\"color:red\">ERROR: HTML validator result no longer in cache.</span>";
            } else {
                html = _cachedResults[i % MAX_CACHED_RESULTS];
                if (html == null) {
                    html = "<span style=\"color:red\">ERROR: Invalid validator result number.</span>";
                } else {
                    i = html.indexOf("<head>");
                    html = html.substring(0, i + 6) + "<base href=\"" + getW3cValidatorCheckUrl() + "\" />" + html.substring(i + 6);
                }
            }
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(html);
        } else {
            renderAndValidate(request, response, chain);
        }
    }

    /**
     * added remove of magnolia main bar as with it the html cannot be valid.
     */
    private void renderAndValidate(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);
        chain.doFilter(request, responseWrapper);
        String contentType = responseWrapper.getContentType();
        if (contentType != null && contentType.startsWith("text/html")) {
            String html = responseWrapper.getBuffer();
            String mgnlHtml = html;
            // remove mgnlMainBar
            if (!StringUtils.isBlank(html) && html.contains(MGNL_MAIN_BAR_BEGIN)) {
                int mgnlMainBarStartPos = html.indexOf(MGNL_MAIN_BAR_BEGIN);
                int mgnlMainBarEndPos = html.indexOf(MGNL_MAIN_BAR_END, mgnlMainBarStartPos);
                html = html.substring(0, mgnlMainBarStartPos) + html.substring(mgnlMainBarEndPos + MGNL_MAIN_BAR_END.length());
            }
            if (!StringUtils.isBlank(html)) {
                html = validateHtml(request, html, mgnlHtml);
                responseWrapper.writeHtml(html);
            }
        } else {
            responseWrapper.writeBuffer();
        }
    }

    private String validateHtml(HttpServletRequest request, String htmlp, String mgnlHtml) {
        String html = htmlp;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking " + request.getRequestURL() + " with W3C Validator ...");
            }
            String validationResult = w3cValidate(request.getRequestURI(), html);

            // Validation error handling ...
            if (validationResult != null) {
                LOGGER.warn("Detected invalid (X)HTML, injecting warning layer into HTML response ...");
                _cachedResults[_resultCounter % MAX_CACHED_RESULTS] = validationResult;
                String validationResultUrl = request.getContextPath() + VALIDATION_RESULT_URL_PREFIX + _resultCounter + VALIDATION_RESULT_URL_SUFFIX;
                // use original html with mgnlMainBar
                if (mgnlHtml.contains(MGNL_MAIN_BAR_BEGIN)) {
                    html = injectWarningLayer(mgnlHtml, validationResultUrl);
                } else {
                    html = injectWarningLayer(html, validationResultUrl);
                }
                _resultCounter++;
            } else {
                // try to put back mgnlMainBar
                if (mgnlHtml.contains(MGNL_MAIN_BAR_END)) {
                    html = mgnlHtml;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Validation failed: " + e.getMessage(), e);
        }
        return html;
    }

    /**
     * Returns <code>null</code>, if no errors could be found,
     * otherwise the HTML of a validation result page is returned.
     */
    private String w3cValidate(String uri, String html) throws IOException {
        boolean isValid = true;
        String validationResult = null;
        HttpClient httpClient = new HttpClient();
        PostMethod w3cValidatorCheck = new PostMethod(getW3cValidatorCheckUrl());
        PartSource bufferSource = new BufferSource(uri, html, "UTF-8");

        FilePart uploadedFile = new FilePart("uploaded_file", bufferSource, "text/html; charset=UTF-8", "UTF-8");
        StringPart showSource = new StringPart("ss", "1");
        Part[] parts = { showSource, uploadedFile }; 
        w3cValidatorCheck.setRequestEntity(new MultipartRequestEntity(parts, w3cValidatorCheck.getParams()));

        int responseCode = httpClient.executeMethod(w3cValidatorCheck);
        if (responseCode != 200) {
            LOGGER.warn(getW3cValidatorCheckUrl() + " responded with " + responseCode + "\n" + getHtml(w3cValidatorCheck));
        } else {
            validationResult = getHtml(w3cValidatorCheck);
            isValid = (validationResult.indexOf("class=\"valid\">This Page Is Valid") != -1);
        }
        return (isValid ? null : validationResult);
    }

    private String getHtml(HttpMethodBase httpMethod) throws IoRuntimeException {
        InputStream in = null;
        String html = "";
        try {
            in = httpMethod.getResponseBodyAsStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            IoTools.copy(in, buffer);
            html = EncodingUtil.getString(buffer.toByteArray(), httpMethod.getResponseCharSet());
        } catch (IOException e) {
            throw new IoRuntimeException(e);
        } finally {
            IoTools.closeQuietly(in);
        }
        return html;        
    }

    private String injectWarningLayer(String html, String validationResultUrl) {
        String lowerCaseHtml = html.toLowerCase();
        int i = lowerCaseHtml.lastIndexOf("</body");
        if (i == -1) {
            i = lowerCaseHtml.lastIndexOf("</html");
        }
        if (i == -1) {
            i = html.length();
        }
        // Insert warning layer before any spaces/tabs preceding </body> ...
        while (i > 0 && (html.charAt(i - 1) == ' ' || html.charAt(i - 1) == '\t')) {
            --i;
        }
        Map<String, Object> replacements = new HashMap<String, Object>();
        replacements.put("validatorWarningCssUri", getValidatorWarningCssUri());
        replacements.put("validationResultUrl", validationResultUrl);
        String warningLayer = StringTools.replacePlaceHolders(_warningLayerTemplate, replacements);
        return html.substring(0, i) + warningLayer + html.substring(i);
    }

    public String getW3cValidatorCheckUrl() {
        return _w3cValidatorCheckUrl;
    }

    public void setW3cValidatorCheckUrl(String w3cValidatorCheckUrl) {
        _w3cValidatorCheckUrl = w3cValidatorCheckUrl;
    }

    public String getValidatorWarningCssUri() {
        return _validatorWarningCssUri;
    }

    public void setValidatorWarningCssUri(String validatorWarningCssUri) {
        _validatorWarningCssUri = validatorWarningCssUri;
    }

    /**
     * @author Michael Tamm
     */
    private static class BufferSource implements PartSource {
        private final String _fileName;
        private final byte[] _buffer;

        public BufferSource(String fileName, String buffer, String encoding) {
            _fileName = fileName;
            if (encoding == null) {
                _buffer = buffer.getBytes();
            } else {
                try {
                    _buffer = buffer.getBytes(encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public long getLength() {
            return _buffer.length;
        }

        public String getFileName() {
            return _fileName;
        }

        public InputStream createInputStream() throws IOException {
            return new ByteArrayInputStream(_buffer);
        }
    }

    /**
     * Adds the method {@link #getContentType}, which is missing
     * in the Servlet 2&#46;3 API, as well as the methods {@link #getBuffer},
     * {@link #writeBuffer}, and {@link #writeHtml}.
     *
     * @author Michael Tamm
     */
    private static class HttpServletResponseWrapper implements HttpServletResponse {
        private final HttpServletResponse _realResponse;
        private BufferServletOutputStream _out = null;
        private BufferPrintWriter _writer = null;
        private String _contentType = null;

        public HttpServletResponseWrapper(HttpServletResponse response) {
            _realResponse = response;
        }

        public void addCookie(Cookie cookie) {
            _realResponse.addCookie(cookie);
        }

        public boolean containsHeader(String name) {
            return _realResponse.containsHeader(name);
        }

        // CHECKSTYLE:OFF
        public String encodeURL(String url) {
            return _realResponse.encodeURL(url);
        }
        // CHECKSTYLE:ON

        // CHECKSTYLE:OFF
        public String encodeRedirectURL(String url) {
            return _realResponse.encodeRedirectURL(url);
        }
        // CHECKSTYLE:ON

        public String encodeUrl(String url) {
            return _realResponse.encodeURL(url);
        }

        public String encodeRedirectUrl(String url) {
            return _realResponse.encodeRedirectURL(url);
        }

        public void sendError(int sc, String msg) throws IOException {
            _realResponse.sendError(sc, msg);
        }

        public void sendError(int sc) throws IOException {
            _realResponse.sendError(sc);
        }

        public void sendRedirect(String location) throws IOException {
            _realResponse.sendRedirect(location);
        }

        public void setDateHeader(String name, long date) {
            _realResponse.setDateHeader(name, date);
        }

        public void addDateHeader(String name, long date) {
            _realResponse.addDateHeader(name, date);
        }

        public void setHeader(String name, String value) {
            _realResponse.setHeader(name, value);
        }

        public void addHeader(String name, String value) {
            _realResponse.addHeader(name, value);
        }

        public void setIntHeader(String name, int value) {
            _realResponse.setIntHeader(name, value);
        }

        public void addIntHeader(String name, int value) {
            _realResponse.addIntHeader(name, value);
        }

        public void setStatus(int sc) {
            _realResponse.setStatus(sc);
        }

        public void setStatus(int sc, String sm) {
            try {
                _realResponse.sendError(sc, sm);
            } catch (IOException e) {
                LOGGER.info("Can not send error message.", e);
            }
        }

        public void setCharacterEncoding(String charset) {
            _realResponse.setCharacterEncoding(charset);
        }

        public String getCharacterEncoding() {
            return _realResponse.getCharacterEncoding();
        }

        public ServletOutputStream getOutputStream() throws IOException {
            if (_writer != null) {
                throw new IllegalStateException("Method getWriter() was already called.");
            }
            if (_out == null) {
                _out = new BufferServletOutputStream();
            }
            return _out;
        }

        public PrintWriter getWriter() throws IOException {
            if (_out != null) {
                throw new IllegalStateException("Method getOutputStream() was already called.");
            }
            if (_writer == null) {
                _writer = new BufferPrintWriter();
            }
            return _writer;
        }

        public void setContentLength(int len) {
            LOGGER.debug("Ignoring setContentLength(" + len + ")");
        }

        public void setContentType(String type) {
            _contentType = type;
            _realResponse.setContentType(type);
        }

        /**
         * Return <code>null</code>, if {@link #setContentType}
         * was not called before.
         */
        public String getContentType() {
            return _contentType;
        }

        public void setBufferSize(int size) {
            _realResponse.setBufferSize(size);
        }

        public int getBufferSize() {
            return _realResponse.getBufferSize();
        }

        public void flushBuffer() throws IOException {
            _realResponse.flushBuffer();
        }

        public void resetBuffer() {
            _realResponse.resetBuffer();
        }

        public boolean isCommitted() {
            return _realResponse.isCommitted();
        }

        public void reset() {
            _realResponse.reset();
        }

        public void setLocale(Locale loc) {
            _realResponse.setLocale(loc);
        }

        public Locale getLocale() {
            return _realResponse.getLocale();
        }

        /**
         * Returns all output written.
         */
        String getBuffer() {
            String html;
            if (_out != null) {
                String encoding = getCharacterEncoding();
                html = _out.getBuffer(encoding);
            } else if (_writer != null) {
                html = _writer.getBuffer();
            } else {
                html = null;
            }
            return html;
        }

        /**
         * Writes the buffered output into the real response.
         */
        void writeBuffer() throws IOException {
            if (_out != null) {
                byte[] bytes = _out.getBytes();
                LOGGER.debug("setContentLength(" + bytes.length + ")");
                _realResponse.setContentLength(bytes.length);
                OutputStream out = _realResponse.getOutputStream();
                out.write(bytes);
            } else if (_writer != null) {
                String text = _writer.getBuffer();
                PrintWriter writer = _realResponse.getWriter();
                writer.print(text);
            }
        }

        /**
         * Writes the given string into the real response.
         */
        void writeHtml(String html) throws IOException {
            if (_out != null) {
                String enconding = getCharacterEncoding();
                byte[] bytes;
                if (enconding == null) {
                    bytes = html.getBytes();
                } else {
                    try {
                        bytes = html.getBytes(enconding);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
                LOGGER.debug("setContentLength(" + bytes.length + ")");
                _realResponse.setContentLength(bytes.length);
                OutputStream out = _realResponse.getOutputStream();
                out.write(bytes);
            } else {
                PrintWriter writer = _realResponse.getWriter();
                writer.print(html);
            }
        }
    }

    /**
     * Writes into a buffer.
     *
     * @author Michael Tamm
     */
    private static class BufferServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream _buffer;

        public BufferServletOutputStream() {
            _buffer = new ByteArrayOutputStream();
        }

        public void write(int b) throws IOException {
            _buffer.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            _buffer.write(b, off, len);
        }

        public byte[] getBytes() {
            return _buffer.toByteArray();
        }

        public String getBuffer(String encoding) {
            String html;
            if (encoding == null) {
                html = _buffer.toString();
            } else {
                try {
                    html = _buffer.toString(encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            return html;
        }
    }

    /**
     * Prints into a buffer.
     *
     * @author Michael Tamm
     */
    private static class BufferPrintWriter extends PrintWriter {
        private final StringWriter _buffer;

        public BufferPrintWriter() {
            this(new StringWriter());
        }

        private BufferPrintWriter(StringWriter buffer) {
            super(buffer);
            _buffer = buffer;
        }

        public String getBuffer() {
            return _buffer.toString();
        }
    }
}