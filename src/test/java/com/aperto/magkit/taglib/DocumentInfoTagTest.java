package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.aperto.magkit.beans.DocumentInfo;
import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubCurrentContent;
import static com.aperto.magkit.mockito.ContentMockUtils.mockContent;
import static com.aperto.magkit.mockito.ContentStubbingOperation.stubNodeData;
import static com.aperto.magkit.mockito.ContentStubbingOperation.stubTitle;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAggregationState;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.HashMap;
import java.util.Map;

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

    @Before
    public void setUp() {
        cleanContext();
        _documentInfoTag = new DocumentInfoTag() {
            @Override
            protected Content retrieveContent(String link) {
                Map<String, String> fileAttributes = new HashMap<String, String>(5);
                fileAttributes.put(FileProperties.PROPERTY_SIZE, Long.toString(FILE_SIZE_BYTE));
                fileAttributes.put(FileProperties.PROPERTY_FILENAME, "testimage");
                fileAttributes.put(FileProperties.PROPERTY_EXTENSION, "jpg");
                return mockContent("document",
                    stubNodeData("document", DocumentInfoTagTest.class.getResourceAsStream("/testimage.jpg"), fileAttributes),
                    stubTitle("title of document")
                );
            }
        };
    }

    @Test
    public void testDocumentInfoTag() throws JspException {
        _documentInfoTag.setNodeDataName("link");
        PageContext pageContext = runLifeCycle(_documentInfoTag);
        DocumentInfo documentInfo = (DocumentInfo) pageContext.getAttribute("documentInfo");
        assertThat(documentInfo.getFileName(), is(FILE_NAME));
        assertThat(documentInfo.getFileExtension(), is(FILE_EXTENSION));
        assertThat(documentInfo.getFileSize(), is(FILE_SIZE_KB));
    }

    @Test
    public void testMbFileSize() throws JspException {
        _documentInfoTag.setNodeDataName("link");
        _documentInfoTag.setFileSize("MB");
        PageContext pageContext = runLifeCycle(_documentInfoTag);
        DocumentInfo documentInfo = (DocumentInfo) pageContext.getAttribute("documentInfo");
        assertThat(documentInfo.getFileSize(), is(FILE_SIZE_MB));
    }

    @Test
    public void testKbFileSize() throws JspException {
        _documentInfoTag.setNodeDataName("link");
        _documentInfoTag.setFileSize("kb");
        PageContext pageContext = runLifeCycle(_documentInfoTag);
        DocumentInfo documentInfo = (DocumentInfo) pageContext.getAttribute("documentInfo");
        assertThat(documentInfo.getFileSize(), is(FILE_SIZE_KB));
    }

    @Test
    public void testByteFileSize() throws JspException {
        _documentInfoTag.setNodeDataName("link");
        _documentInfoTag.setFileSize("byte");
        PageContext pageContext = runLifeCycle(_documentInfoTag);
        DocumentInfo documentInfo = (DocumentInfo) pageContext.getAttribute("documentInfo");
        assertThat(documentInfo.getFileSize(), is(FILE_SIZE_BYTE));
    }

    @Test
    public void testWrongNodedata() throws JspException {
        _documentInfoTag.setNodeDataName("link2");
        PageContext pageContext = runLifeCycle(_documentInfoTag);
        DocumentInfo documentInfo = (DocumentInfo) pageContext.getAttribute("documentInfo");
        assertThat(documentInfo, nullValue());
    }

    @Override
    protected PageContext createPageContext() {
        Content mockContent = mockContent("test",
            stubNodeData("link", "http://www.aperto.de/test.html"),
            // mock behaviour of mgnl default content -> NullObject
            stubNodeData("link2", "")
        );
        mockAggregationState(stubCurrentContent(mockContent));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("contentObj", mockContent);
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        request.setContextPath("/author");

        MockHttpServletResponse response = new MockHttpServletResponse();
        return new MockPageContext(new MockServletContext(), request, response);
    }
}
