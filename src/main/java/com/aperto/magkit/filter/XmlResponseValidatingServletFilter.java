package com.aperto.magkit.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;
import org.xml.sax.SAXException;

/**
 * Validates a xml formatted response against a configured xml schema.
 * It may be configured to fail if the xml is invalid regarding the schema. Furthermore a info about the validity of
 * the response may be appended to the orginal response after validation.
 * <p/>
 * The validation occurres on a buffer of the current response. This may delay the answer of an request and needs
 * additional memory.
 * <p/>
 * TODO: Currently only the usage of response writer and not stream is supported.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class XmlResponseValidatingServletFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(XmlResponseValidatingServletFilter.class);
    private Schema _schema;
    private boolean _failOnInvalidXml = false;
    private boolean _appendValidationInfo = true;
    private String _schemaPath;

    public void setFailOnInvalidXml(final Boolean failOnInvalidXml) {
        _failOnInvalidXml = failOnInvalidXml != null ? failOnInvalidXml : false;
    }

    public void setAppendValidationInfo(final Boolean appendValidationInfo) {
        _appendValidationInfo = appendValidationInfo != null ? appendValidationInfo : false;
    }

    public void setSchemaPath(final String schemaPath) {
        _schemaPath = schemaPath;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        ResponseWrapper responseWrapper = new ResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);
        String validationMessage = validate(responseWrapper);
        if (validationMessage == null || !_failOnInvalidXml) {
            PrintWriter writer = response.getWriter();
            writer.write(responseWrapper.getBuffer());
            if (_appendValidationInfo) {
                writer.write("<!-- ");
                writer.write("valid:" + (validationMessage == null));
                if (validationMessage != null) {
                    writer.write(" error:" + validationMessage);
                }
                writer.write(" --> ");
            }
        } else {
            throw new RuntimeException("Invalid XML.");
        }
    }

    /**
     * Returns {@code null} if the response is valid, otherwise a message that describes the error.
     */
    String validate(final ResponseWrapper responseWrapper) throws IOException {
        String message;
        try {
            String buffer = responseWrapper.getBuffer();
            Reader reader = new StringReader(buffer);
            Source source = new StreamSource(reader);
            getSchemaValidator().validate(source);
            message = null;
        } catch (SAXException e) {
            message = e.getMessage();
            LOGGER.error("Invalid XML response.", e);
        }
        return message;
    }

    private synchronized Validator getSchemaValidator() throws SAXException {
        if (_schema == null) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream stream = XmlResponseValidatingServletFilter.class.getResourceAsStream(_schemaPath);
            Source schemaFile = new StreamSource(stream);
            _schema = factory.newSchema(schemaFile);
        }
        return _schema.newValidator();
    }

    /**
     * Wrapper for an {@link HttpServletResponse} that provides a buffering of the response content.
     */
    public static class ResponseWrapper extends HttpServletResponseWrapper {

        private StringWriter _writer;
        private ByteArrayOutputStream _outputStream;

        ResponseWrapper(final HttpServletResponse servletResponse) {
            super(servletResponse);
            _writer = new StringWriter();
            _outputStream = new ByteArrayOutputStream();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return new BufferedServletOutputStream(_outputStream);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(_writer);
        }

        public String getBuffer() {
            // TODO _outputStream.toString();
            return _writer.getBuffer().toString();
        }
    }

    /**
     * Just to make a simple {@link java.io.OutputStream} a {@link ServletOutputStream}.
     */
    public static class BufferedServletOutputStream extends ServletOutputStream {

        private OutputStream _outputStream;

        public BufferedServletOutputStream(final OutputStream outputStream) {
            _outputStream = outputStream;
        }

        public void write(final int b) throws IOException {
            _outputStream.write(b);
        }
    }
}