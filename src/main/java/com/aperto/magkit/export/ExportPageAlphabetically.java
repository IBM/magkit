package com.aperto.magkit.export;

import com.aperto.webkit.utils.ExceptionEater;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.pages.ExportPage;
import org.apache.commons.lang.StringUtils;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Extends {@link ExportPage} so that the export xml will use alphabetic
 * ordering for sv:property elements inside sv:node elements. The sv:property
 * elements will be ordered by their sv:name attribute.
 *
 * @author reik.schatz
 */
public class ExportPageAlphabetically extends ExportPage {

    /**
     * Simply calls the constructor of {@link ExportPage}.
     */
    public ExportPageAlphabetically(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Exports a XML structure whose nodes are always sorted alphabetically after the
     * value in the @sv:name attribute.
     */
    @Override
    public String exportxml() throws Exception {
        setDefaults();
        if (!checkPermissions(getRequest(), getMgnlRepository(), getMgnlPath(), Permission.WRITE)) {
            throw new ServletException(new AccessDeniedException("Write permission needed for export. User not allowed to WRITE path [" + getMgnlPath() + "]"));
        }
        HierarchyManager hr = MgnlContext.getHierarchyManager(getMgnlRepository());
        Workspace ws = hr.getWorkspace();
        Session session = ws.getSession();

        if (getExt().equalsIgnoreCase(DataTransporter.ZIP)) {
            getResponse().setContentType(MIME_APPLICATION_ZIP);
        } else if (getExt().equalsIgnoreCase(DataTransporter.GZ)) {
            getResponse().setContentType(MIME_GZIP);
        } else {
            getResponse().setContentType(MIME_TEXT_XML);
            getResponse().setCharacterEncoding("UTF-8");
        }

        String pathName = StringUtils.replace(getMgnlPath(), "/", ".");
        if (".".equals(pathName)) {
            pathName = StringUtils.EMPTY;
        }
        getResponse().setHeader("content-disposition", "attachment; filename=" + getMgnlRepository() + pathName + getExt());
        ByteArrayOutputStream memoryOutputStream = new ByteArrayOutputStream();

        XMLWriter w = null;
        try {
            DataTransporter.executeExport(memoryOutputStream, isMgnlKeepVersions(), isMgnlFormat(), session, getMgnlPath(), getMgnlRepository(), getExt());
            Document document = parse(memoryOutputStream);
            OutputStream baseOutputStream = getResponse().getOutputStream();
            OutputFormat prettyPrint = OutputFormat.createPrettyPrint();
            prettyPrint.setEncoding("UTF-8");
            w = new XMLWriter(baseOutputStream, prettyPrint);
            w.write(document);
            w.flush();

        } catch (RuntimeException e) {
            getResponse().setContentType("text/html; charset=UTF-8");
            getResponse().setHeader("content-disposition", "inline");
            throw e;
        } finally {
            if (w != null) {                
                w.close();
            }
        }
        return VIEW_EXPORT;
    }

    private Document parse(ByteArrayOutputStream outputStream) {
        Document document = null;
        try {
            document = DocumentHelper.parseText(outputStream.toString("UTF-8"));
        } catch (DocumentException e) {
            ExceptionEater.eat(e);
        } catch (UnsupportedEncodingException e) {
            ExceptionEater.eat(e);            
        }

        if (document != null) {
            List list = document.selectNodes("//sv:node[@sv:name]");
            for (Object aList : list) {
                Element parentNode = (Element) aList;

                List properties = parentNode.selectNodes("sv:property");
                Map<String, Node> sortedProperties = new TreeMap<String, Node>();
                for (Object property1 : properties) {
                    Node property = (Node) property1;
                    // select parentNode name
                    String name = property.valueOf("@sv:name");
                    // sort nodes after parentNode name                      
                    sortedProperties.put(name, property);
                    // detach original parentNode from parent
                    property.detach();
                }

                List<Node> remainingNodes = new ArrayList<Node>();
                for (Object o : parentNode.elements()) {
                    Node child = (Node) o;
                    remainingNodes.add(child);
                    child.detach();
                }

                // re-attach sorted properties first
                for (Map.Entry<String, Node> entry : sortedProperties.entrySet()) {
                    parentNode.add(entry.getValue());
                }
                // re-attach remaining nodes in original order
                for (Object remainingNode : remainingNodes) {
                    Node child = (Node) remainingNode;
                    parentNode.add(child);
                }
            }
        }
        return document;
    }

    private void setDefaults() {
        if (StringUtils.isEmpty(getMgnlRepository())) {
            setMgnlRepository(ContentRepository.WEBSITE);
        }
        if (StringUtils.isEmpty(getMgnlPath())) {
            setMgnlPath("/");
        }
        if (StringUtils.isEmpty(getExt())) {
            setExt(DataTransporter.XML);
        }
    }
}
