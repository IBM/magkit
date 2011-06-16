package com.aperto.magkit.utils;

import info.magnolia.cms.core.*;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.*;

import javax.jcr.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

import static java.io.File.createTempFile;
import static java.util.Collections.sort;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;

/**
 * Util class for handle magnolia content.
 *
 * @author frank.sommer (15.05.2008)
 */
public final class ContentUtils {
    /**
     * Orders the given collection of NodeDatas by name.
     *
     * @param collection of NodeDatas
     * @return ordered collection
     */
    public static List<NodeData> orderNodeDataCollection(Collection<NodeData> collection) {
        List<NodeData> nodeDataList = (List<NodeData>) collection;
        NodeDataComparator nodeDataComparator = new NodeDataComparator();
        nodeDataComparator.setCompareByValue(false);
        sort(nodeDataList, nodeDataComparator);
        return nodeDataList;
    }

    private ContentUtils() {
    }

    public static List<NodeData> orderNodeDataCollectionByValue(Collection<NodeData> collection) {
        List<NodeData> nodeDataList = (List<NodeData>) collection;
        sort(nodeDataList, new NodeDataComparator());
        return nodeDataList;
    }

    /**
     * Session based copy operation. As JCR only supports workspace based copies this operation is performed
     * by using export import operations.
     * <p/>
     * Implementation based on {@link info.magnolia.cms.util.ContentUtil}.
     */
    public static void copyInSessionFiltered(Content src, String dest, XMLFilter... filters) throws RepositoryException {
        final String destParentPath = defaultIfEmpty(substringBeforeLast(dest, "/"), "/");
        final String destNodeName = substringAfterLast(dest, "/");
        final Session session = src.getWorkspace().getSession();
        try {
            final File file = createTempFile("mgnl", null, Path.getTempDirectory());
            final FileOutputStream outStream = new FileOutputStream(file);
            try {
                ContentHandler handler = getExportContentHandler(outStream);
                SystemViewExportXmlReaderAdapter adapter = new SystemViewExportXmlReaderAdapter(session, false, false);
                if (filters != null && filters.length > 0) {
                    XMLReader lastFilter = adapter;
                    for (XMLFilter filter : filters) {
                        filter.setParent(lastFilter);
                        lastFilter = filter;
                    }
                    lastFilter.setContentHandler(handler);
                    lastFilter.parse(src.getHandle());
                } else {
                    adapter.setContentHandler(handler);
                    adapter.parse(src.getHandle());
                }
            } catch (SAXException e) {
                Exception exception = e.getException();
                if (exception instanceof RepositoryException) {
                    throw (RepositoryException) exception;
                } else if (exception instanceof IOException) {
                    throw (IOException) exception;
                } else {
                    throw new RepositoryException("Error serializing system view XML", e);
                }
            }
            outStream.flush();
            closeQuietly(outStream);
            FileInputStream inStream = new FileInputStream(file);
            session.importXML(destParentPath, inStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
            closeQuietly(inStream);
            file.delete();
            if (!StringUtils.equals(src.getName(), destNodeName)) {
                String currentPath = destParentPath.equals("/") ?
                    "/" + src.getName()
                    : destParentPath + "/" + src.getName();
                session.move(currentPath, dest);
            }
        } catch (IOException e) {
            throw new RepositoryException("Can't copy node " + src + " to " + dest, e);
        }
    }

    /**
     * Creates a {@link ContentHandler} instance that serializes the
     * received SAX events to the given output stream.
     * <p/>
     * Implementation copied from {@link org.apache.jackrabbit.commons.AbstractSession} jackrabbit-jcr-commons-1.4.
     *
     * @param stream output stream to which the SAX events are serialized
     * @return SAX content handler
     * @throws RepositoryException if an error occurs
     */
    private static ContentHandler getExportContentHandler(OutputStream stream)
        throws RepositoryException {
        try {
            SAXTransformerFactory stf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler handler = stf.newTransformerHandler();
            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            handler.setResult(new StreamResult(stream));
            return handler;
        } catch (TransformerFactoryConfigurationError e) {
            throw new RepositoryException(
                "SAX transformer implementation not available", e);
        } catch (TransformerException e) {
            throw new RepositoryException(
                "Error creating an XML export content handler", e);
        }
    }
}