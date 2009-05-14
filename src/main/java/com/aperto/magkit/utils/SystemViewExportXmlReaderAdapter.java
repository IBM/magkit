package com.aperto.magkit.utils;

import java.io.IOException;
import java.io.OutputStream;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * TODO: comment.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class SystemViewExportXmlReaderAdapter implements XMLReader {

    private ContentHandler _contentHandler;
    private final Session _session;
    private final boolean _skipBinary;
    private final boolean _noRecurse;

    public SystemViewExportXmlReaderAdapter(final Session session, final boolean skipBinary, final boolean noRecurse) {
        _session = session;
        _skipBinary = skipBinary;
        _noRecurse = noRecurse;
    }

    public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    public void setFeature(final String name, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    public void setProperty(final String name, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public void setEntityResolver(final EntityResolver resolver) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setDTDHandler(final DTDHandler handler) {
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public void setContentHandler(final ContentHandler handler) {
        _contentHandler = handler;
    }

    public ContentHandler getContentHandler() {
        return _contentHandler;
    }

    public void setErrorHandler(final ErrorHandler handler) {
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public void parse(final InputSource input) throws IOException, SAXException {
        parse(input.getSystemId());
    }

    public void parse(final String systemId) throws IOException, SAXException {
        try {
            _session.exportSystemView(systemId, _contentHandler, _skipBinary, _noRecurse);
        } catch (RepositoryException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
}