package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.aperto.magkit.mock.MockContent;
import com.aperto.magkit.mock.MockNodeData;
import com.aperto.magkit.beans.DocumentInfo;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.*;
import javax.jcr.RepositoryException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * @author diana.racho (29.04.2008)
 */
public class DocumentInfoTagTest extends MagKitTagTest {
    private static final String FILE_NAME = "testimage";
    private static final String FILE_EXTENSION = "jpg";
    private static final long FILE_SIZE_KB = 1024L;
    private static final long FILE_SIZE_BYTE = 1048576L;
    private static final long FILE_SIZE_MB = 1L;
    private DocumentInfoTag _documentInfoTag;

    @Test
    public void testDocumentInfoTag() throws JspException {
        _documentInfoTag.setNodeDataName("link");
        PageContext pageContext = runLifeCycle(_documentInfoTag);
        ServletRequest request = pageContext.getRequest();
        DocumentInfo documentInfo = (DocumentInfo) request.getAttribute("documentInfo");
        assertThat(documentInfo.getFileName(), is(FILE_NAME));
        assertThat(documentInfo.getFileExtension(), is(FILE_EXTENSION));
        assertThat(documentInfo.getFileSize(), is(FILE_SIZE_KB));
    }

    @Test
    public void testMbFileSize() throws JspException {
        _documentInfoTag.setNodeDataName("link");
        _documentInfoTag.setFileSize("MB");
        PageContext pageContext = runLifeCycle(_documentInfoTag);
        ServletRequest request = pageContext.getRequest();
        DocumentInfo documentInfo = (DocumentInfo) request.getAttribute("documentInfo");
        assertThat(documentInfo.getFileSize(), is(FILE_SIZE_MB));
    }

    @Test
    public void testKbFileSize() throws JspException {
        _documentInfoTag.setNodeDataName("link");
        _documentInfoTag.setFileSize("kb");
        PageContext pageContext = runLifeCycle(_documentInfoTag);
        ServletRequest request = pageContext.getRequest();
        DocumentInfo documentInfo = (DocumentInfo) request.getAttribute("documentInfo");
        assertThat(documentInfo.getFileSize(), is(FILE_SIZE_KB));
    }

    @Test
    public void testByteFileSize() throws JspException {
        _documentInfoTag.setNodeDataName("link");
        _documentInfoTag.setFileSize("byte");
        PageContext pageContext = runLifeCycle(_documentInfoTag);
        ServletRequest request = pageContext.getRequest();
        DocumentInfo documentInfo = (DocumentInfo) request.getAttribute("documentInfo");
        assertThat(documentInfo.getFileSize(), is(FILE_SIZE_BYTE));
    }

    @Test
    public void testWrongNodedata() throws JspException {
        _documentInfoTag.setNodeDataName("link2");
        PageContext pageContext = runLifeCycle(_documentInfoTag);
        ServletRequest request = pageContext.getRequest();
        DocumentInfo documentInfo = (DocumentInfo) request.getAttribute("documentInfo");
        assertThat(documentInfo, nullValue());
    }

    @Override
    protected PageContext createPageContext() {
        MockContent mockContent = new MockContent("test", ItemType.CONTENT);
        MockNodeData mockNodeData = new MockNodeData("link", "http://www.aperto.de/test.html");
        mockContent.addNodeData(mockNodeData);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("contentObj", mockContent);
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        request.setContextPath("/author");

        MockHttpServletResponse response = new MockHttpServletResponse();
        // init MgnlContext:
        initMgnlWebContext(request, response, httpSession.getServletContext());
        MgnlContext.getAggregationState().setCurrentContent(mockContent);
        return new MockPageContext(new MockServletContext(), request, response);
    }

    @Before
    public void getDocumentInfoTag() {
        _documentInfoTag = new DocumentInfoTag() {
            @Override
            protected Content retrieveContent(String link) {
                MockContent mockcontent2 =  new MockContent("document", ItemType.CONTENT);
                MockNodeData mockNodeData2 = new MockNodeData("document", DocumentInfoTagTest.class.getResourceAsStream("/testimage.jpg"));
                try {
                    mockNodeData2.setAttribute(FileProperties.PROPERTY_SIZE, Long.toString(FILE_SIZE_BYTE));
                    mockNodeData2.setAttribute(FileProperties.PROPERTY_FILENAME, "testimage");
                    mockNodeData2.setAttribute(FileProperties.PROPERTY_EXTENSION, "jpg");
                } catch (RepositoryException e) {
                    fail("Can not set attributes.");
                }
                mockcontent2.addNodeData(mockNodeData2);
                return mockcontent2;
            }
        };
    }
}
