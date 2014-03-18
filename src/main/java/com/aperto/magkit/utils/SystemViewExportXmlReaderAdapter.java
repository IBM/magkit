package com.aperto.magkit.utils;

import org.xml.sax.*;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;

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