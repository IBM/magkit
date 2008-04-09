package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Test of the convert link tag.
 *
 * @author frank.sommer (10.01.2008)
 */
public class ConvertLinkTagTest extends MagKitTagTest {
    private static final String LOCAL_CONTENT_OBJ = "contentObj";
    private static final String CONTEXT_PATH = "/author";
    private static final String LINK_VALUE_EXT = "http://www.aperto.de";
    private static final String LINK_VALUE_INT = "/sammeln/infos";

    public void testExternalLink() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link");
        PageContext pageContext = runLifeCycle(tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertFalse(output.contains(CONTEXT_PATH));
        assertTrue(output.contains(LINK_VALUE_EXT));
    }

    public void testInternalLink() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link2");
        PageContext pageContext = runLifeCycle(tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertTrue(output.contains(CONTEXT_PATH));
        assertTrue(output.contains(LINK_VALUE_INT));
    }

    @Override
    protected PageContext createPageContext() {
        MockContent mockContent = new MockContent("test", ItemType.CONTENT);
        MockNodeData mockNodeData = new MockNodeData("link", LINK_VALUE_EXT);
        mockContent.addNodeData(mockNodeData);
        mockNodeData = new MockNodeData("link2", LINK_VALUE_INT);
        mockContent.addNodeData(mockNodeData);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(LOCAL_CONTENT_OBJ, mockContent);
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        request.setContextPath(CONTEXT_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();
        // init MgnlContext:
        initMgnlWebContext(request, response, httpSession.getServletContext());
        MgnlContext.getAggregationState().setCurrentContent(mockContent);
        return new MockPageContext(new MockServletConfig(), request, response);
    }
}